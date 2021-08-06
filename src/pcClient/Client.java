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
import transponderTCP.ClientMessage;
import transponderTCP.ServerMessage;
import transponderTCP.TransponderTCP;

public class Client implements Runnable{
	
	private Socket clientSock = null;
	private TransponderTCP clientTransp = null;
	private Thread transpThread = null;
	
	pcConsole.Console mainConsole = null;

	private int battleTagCode = 0;
	private String battleTag = "";
	
	private Scanner inpScanner = null;
	private ChatState chatState = null;
	private boolean stopFlag;
	
	public Client() {
		menuPrompt();
		clientTransp = new TransponderTCP(this.clientSock);
		this.transpThread = new Thread(clientTransp);
		this.inpScanner = new Scanner(System.in);
	}
	
	
	public Client(Socket clientSock, boolean debug) {
		this.clientSock = clientSock;
		this.clientTransp = new TransponderTCP(this.clientSock);
		this.clientTransp.setDebugFlag(debug);
		this.transpThread = new Thread(clientTransp);
		this.inpScanner = new Scanner(System.in);
	}

	public static void main(String[] args) {
		Thread s = Thread.currentThread();
		s.setName("PC-Client Main");
		
		Boolean stopFlag = false;
		
		Scanner userInp = new Scanner(System.in);
		
		Client initClient = new Client();
		
		Thread clientThread = new Thread(initClient);
		clientThread.setName("PC-Client");
		
		clientThread.start();
		
		while(stopFlag == false) {
			System.out.println(initClient.getChatState());
			System.out.println("Type 'x' to exit! ");
			System.out.println("Enter message to send to chat:");
			
			String message = userInp.nextLine();
			
			if(message.compareTo("x") == 0) {
				
				stopFlag = true;
				
				initClient.clientTransp.stopAll();
				
				break;
			}

			initClient.sendChatMessage(message);
		}
		
	}
	
	public void menuPrompt() {
		
		this.inpScanner = new Scanner(System.in);
		
		String intro = "PlatChat Client interface. \n";

		String prompt1 = "Please enter the IP address this client should connect via:";
		String prompt2 = "Please enter the Socket this client should connect via:";
		String prompt3 = "Please enter the IP address this client should connect to:";
		String prompt4 = "Please enter the Remote Socket this client should connect to:";
		String prompt5 = "Please enter your BattleTag (Excluding the code):";
		String prompt6 = "Please enter your BattleTag Code (Ex: #1234)";

		System.out.println(intro);
		
		// Prompt for client connect ip
		System.out.println(prompt1);
		String ipAddrInp = inpScanner.nextLine();

		// prompt for client connect port
		System.out.println(prompt2);
		int ipPort = Integer.valueOf(inpScanner.nextLine());

		// Prompt for client connect ip
		System.out.println(prompt3);
		String ipAddrInpServ = inpScanner.nextLine();
		
		// prompt for server listen port
		System.out.println(prompt4);
		int ipPortServ = Integer.valueOf(inpScanner.nextLine());
		
		// prompt for player battletag
		System.out.println(prompt5);
		this.battleTag = inpScanner.nextLine();

		// prompt for player battletag code
		System.out.println(prompt6);
		this.battleTagCode = Integer.valueOf(inpScanner.nextLine());

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
		
		if(this.transpThread == null) {
			throw new IllegalStateException("pcClient| Transponder thread cannot be null!");
		}
		
		this.transpThread.start();
		
		while(this.stopFlag == false) {
			if(this.clientTransp.getNewSMFlag()) {
				
				// Need to fix transponder so we can pull the recieved ServerMessages
				try {
					ServerMessage<?> inputSM = null;

					inputSM = this.clientTransp.getLastSM();
					
					// If the server sends us a ChatState: Accept it, and print!
					if(inputSM instanceof ChatState) {

						this.chatState = (ChatState)inputSM;
						this.mainConsole.printToConsole(this.chatState.toString());
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(this.clientTransp.getNewCMFlag()) {
				ClientMessage<?> inputCM = null;
				
				try {
					// TODO: At the moment, this simply prints recieved ClientMessages to Console
					// Do something better with it in the future?
					inputCM = this.clientTransp.getLastCM();
					
					this.mainConsole.printToConsole(inputCM.toString());
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void sendChatMessage(String messageInp) {
		
		if(this.clientSock.isConnected()) {
			
			ChatMessage message = new ChatMessage(messageInp);
			
			this.clientTransp.sendClientMessage(message);
		}
	}
	
	public ChatState getChatState() {
		return this.chatState;
	}
	
	public ChatState getLatestChatState() {
		return this.chatState;
	}
	
	public String printChatState(ChatState input) {
		return input.toString();
	}
	
	public String debugGetLastClientMessage() {
		String result = "";
		
		try {
			result += this.clientTransp.getLastCM() + "\n";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void setConsole(pcConsole.Console inpConsole) {
		this.mainConsole = inpConsole;
	}
	
	public void printMessages() {
		System.out.println(clientTransp.getClientStream());
	}

}
