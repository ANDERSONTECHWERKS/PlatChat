package pcServer;
import transponderTCP.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Server implements Runnable{
	String welcomeMessage = "PLATCHAT_EXPERIMENTAL! WELCOME!";

	ServerSocket serverSock = null;
	
	Socket clientSock = null;
	
	TransponderTCP servTransponder = null;
	Thread transponderThread = null;
	
	HashSet<Socket> clientSockets = null;
	
	boolean debugFlag = false;
	boolean stopFlag = false;
	
	public Server() {
		this.menuPrompt();
		this.servTransponder = new TransponderTCP(this.serverSock);
		ChatState pcState = new ChatState();
	}
	
	public Server(ServerSocket servSock, boolean debug) {
		
		this.serverSock = servSock;
		this.servTransponder = new TransponderTCP(this.serverSock);
		
		ChatState initPCState = new ChatState();
		
		ChatMessage welcomeMessage = new ChatMessage(this.welcomeMessage);
		
		initPCState.addMessage(welcomeMessage);
		
		this.servTransponder.setDebugFlag(debug);
		
		this.servTransponder.setServerMessage(initPCState);
		
		this.transponderThread = new Thread(this.servTransponder);
	}
	
	
	
	public void listen() {
		this.transponderThread.start();
	}
	
	public static boolean isPlayerPlat(String battleTag, int btCode, boolean debugFlag) {

		// Craft the custom URL based off of the playoverwatch.com address format
		URL playOW = null;
		BufferedReader webInput = null;
		int playerHighestSR = 0;
		
		try {

			playOW = new URL("https://playoverwatch.com/en-us/career/pc/" + battleTag + "-" + btCode);

			webInput = new BufferedReader(new InputStreamReader(playOW.openStream()));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String webPageLine;

		// Figure out what data structure you want to place that webPageLine string into.
		
		try {
			//TODO: This is the ugliest way I could have found to parse this. This needs review and improving!
			// The point is to actually read the rest of the line after we find the wordds "competitive-rank-level"
			// currently: We just find the first and last reference, and pray that we get what we want.
			
			while ((webPageLine = webInput.readLine()) != null) {
				
				if(webPageLine.contains("competitive-rank-level")) {
					
					int lineMarkerCRL = webPageLine.indexOf("competitive-rank-level");
					String SR = webPageLine.substring(lineMarkerCRL + 24, lineMarkerCRL + 28);
					Integer SRInt = Integer.valueOf(SR);
					
					// Always assign HighestSR found
					if(SRInt > playerHighestSR) {
						playerHighestSR = SRInt;
					}
					
					int lineMarkerNext = webPageLine.lastIndexOf("competitive-rank-level");
					SR = webPageLine.substring(lineMarkerNext + 24, lineMarkerNext + 28);
					SRInt = Integer.valueOf(SR);
					
					// Always assign HighestSR found
					if(SRInt > playerHighestSR) {
						playerHighestSR = SRInt;
					}
					
					
					System.out.println("Highest detected SR for player " + battleTag + "#" + btCode + " is: \n" + SRInt);
				}
				
				if(debugFlag == true) {
					System.out.println(webPageLine);
				}
				
			}
			
			// Check if platinum!
			if(playerHighestSR < 3000 && playerHighestSR >= 2500) {
				
				if(debugFlag == true) {
					System.out.println("Player "+ battleTag + "#" + btCode + " is Platinum!");
				}
				
				return true;
			}
			
			webInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server();
	}

	public void menuPrompt() {
		String intro = "PlatChat server interface. \n";
		Scanner inpScanner = new Scanner(System.in);

		String prompt1 = "Please enter the IP address this server should listen on:";
		String prompt2 = "Please enter the Socket this server should listen on:";
		String prompt3 = "Please enter your Battle Tag (Excluding the code):";
		String prompt4 = "Please enter your Battle Tag Code (Ex: #1234)";

		System.out.println(intro);
		
		// Prompt for server listen ip
		System.out.println(prompt1);
		String ipAddrInp = inpScanner.nextLine();

		// prompt for server listen socket
		System.out.println(prompt2);
		int ipPort = inpScanner.nextInt();

		// prompt for player battletag
		System.out.println(prompt3);
		String battleTag = inpScanner.nextLine();

		// prompt for player battletag code
		System.out.println(prompt4);
		int battleTagCode = 0;

		this.serverSock = this.createServerSock(ipAddrInp, ipPort, 12);
	}

	public ServerSocket createServerSock(String listenAddr, int port, int backlog) {

		// First, try to convert the ip address into an iNetAddress object
		InetAddress iNetAddr = null;
		try {
			iNetAddr = InetAddress.getByName(listenAddr);
		} catch (UnknownHostException e1) {
			System.out.println("pcServer| Bad input IP adddress!");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Next, attempt creating the Server Socket we need.

		try {

			this.serverSock = new ServerSocket(port, backlog, iNetAddr);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serverSock;
	}
	
	public ArrayList<ClientMessage<?>> getLastMessages() {
		ArrayList<ClientMessage<?>> result = null;
		
		result = this.servTransponder.serverRetrieveMessages();
		
		System.out.println(result.toString());
		
		return result;
	}

	@Override
	public void run() {
		listen();
		
	}

}
