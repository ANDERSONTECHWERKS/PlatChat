package pcClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import pcServer.*;
import transponderTCP.TransponderTCP;

public class Client implements Runnable{
	
	Socket clientSock = null;
	TransponderTCP clientTransp = null;
	Thread transpThread = null;
	
	int battleTagCode = 0;
	String battleTag = "";
	
	public Client() {
		menuPrompt();
		clientTransp = new TransponderTCP(this.clientSock);
		this.transpThread = new Thread(clientTransp);
	}
	
	public Client(Socket clientSock, boolean debug) {
		this.clientSock = clientSock;
		this.clientTransp = new TransponderTCP(this.clientSock);
		this.clientTransp.setDebugFlag(debug);
		this.transpThread = new Thread(clientTransp);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void menuPrompt() {
		String intro = "PlatChat Client interface. \n";
		Scanner inpScanner = new Scanner(System.in);

		String prompt1 = "Please enter the IP address this client should connect via:";
		String prompt2 = "Please enter the Socket this client should connect via:";
		String prompt3 = "Please enter the IP address this client should connect via:";
		String prompt4 = "Please enter the Socket this client should connect via:";
		String prompt5 = "Please enter your Battle Tag (Excluding the code):";
		String prompt6 = "Please enter your Battle Tag Code (Ex: #1234)";

		System.out.println(intro);
		
		// Prompt for client connect ip
		System.out.println(prompt1);
		String ipAddrInp = inpScanner.nextLine();

		// prompt for client connect port
		System.out.println(prompt2);
		int ipPort = inpScanner.nextInt();

		// Prompt for client connect ip
		System.out.println(prompt3);
		String ipAddrInpServ = inpScanner.nextLine();
		
		// prompt for server listen port
		System.out.println(prompt4);
		int ipPortServ = inpScanner.nextInt();
		
		// prompt for player battletag
		System.out.println(prompt5);
		this.battleTag = inpScanner.nextLine();

		// prompt for player battletag code
		System.out.println(prompt6);
		this.battleTagCode = 0;

		// attempt to create the clientSock object
		try {
			this.clientSock = new Socket(InetAddress.getByName(ipAddrInpServ),ipPortServ,InetAddress.getByName(ipAddrInp), ipPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.transpThread.start();
	}
	
	public void sendMessage(String messageInp) {
		
		if(this.clientSock.isConnected()) {
			
			ChatMessage message = new ChatMessage(messageInp);
			
			this.clientTransp.clientSendMessage(message);
		}
	}
	
	public String debugGetLastChatMessage() {
		String result = "";
		
		try {
			result += this.clientTransp.clientGetLastMessage() + "\n";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void printMessages() {
		System.out.println(clientTransp.getClientStream());
	}

}
