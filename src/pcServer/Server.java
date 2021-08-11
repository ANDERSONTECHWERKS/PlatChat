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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

import pcConsole.Console;

public class Server implements Runnable {
	String welcomeMessage = "PLATCHAT_EXPERIMENTAL! WELCOME!";

	ChatState chatState = null;

	pcConsole.Console mainConsole = null;

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

		this.transponderThread = new Thread(servTransponder);

		ChatState pcState = new ChatState(this.welcomeMessage);
		this.chatState = pcState;

		this.servTransponder.setInitServerMessage(pcState);

	}

	// Constructor that should help us debug
	public Server(String serverIP, int port, int backlog, boolean debug) {

		this.serverSock = this.createServerSock(serverIP, port, backlog);

		this.servTransponder = new TransponderTCP(this.serverSock);

		this.servTransponder.setDebugFlag(debug);

		ChatState pcState = new ChatState(this.welcomeMessage);
		this.chatState = pcState;

		this.servTransponder.setInitServerMessage(pcState);

		this.transponderThread = new Thread(servTransponder);

	}

	public Server(ServerSocket servSock) {

		this.serverSock = servSock;

		this.servTransponder = new TransponderTCP(this.serverSock);

		ChatState pcState = new ChatState(this.welcomeMessage);
		this.chatState = pcState;

		this.servTransponder.setInitServerMessage(pcState);

		this.transponderThread = new Thread(servTransponder);

	}

	public Server(ServerSocket servSock, boolean debug) {

		this.serverSock = servSock;

		this.servTransponder = new TransponderTCP(this.serverSock);

		ChatState pcState = new ChatState(this.welcomeMessage);
		this.chatState = pcState;

		// Initialize first chatState with welcome message, set payload.
		this.servTransponder.setDebugFlag(debug);

		this.servTransponder.setInitServerMessage(pcState);

		this.transponderThread = new Thread(this.servTransponder);
	}

	// Sends a chat message to all clients connected to TransponderTCP instance

	public void sendChatMessage(String messageInp) {
		// TODO: This method is bare-minimum. Finish it!

		ChatMessage message = new ChatMessage(messageInp);

		this.updateChatState(message);
		this.servTransponder.sendClientMessageToAll(message);
		this.servTransponder.sendServerMessageToAll(chatState);
	}

	public void sendChatState(ChatState inpState) {
		this.servTransponder.sendServerMessageToAll(inpState);
	}

	// Send a server message to all clients connected to TransponderTCP instance
	public void sendServerMessage(ServerMessage<?> messageInp) {

		this.servTransponder.sendServerMessageToAll(messageInp);
	}

	public void listen() {

		while (this.stopFlag == false) {

			ClientMessage<?> inpCM = null;
			ServerMessage<?> inpSM = null;

			if (this.servTransponder.getNewCMFlag()) {

				try {
					inpCM = this.servTransponder.getLastCM();

					if (this.debugFlag == true) {
						System.out.println("pc-Server| Pulling last ClientMessage from Transponder...");
						System.out.println("pc-Server| Last ClientMessage reported from Transponder is: \n "
								+ inpCM.toString() + "\n");
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// If the incoming ClientMessage is a ChatMessage - just print it to the console
				// TODO: Handle this better in the future
				if (inpCM instanceof ChatMessage) {

					ChatMessage castChMess = (ChatMessage) inpCM;

					if (this.debugFlag == true) {
						System.out.println("pc-Server| Recieved ChatMessage object in listen() loop!");
					}

					// Idea: If we get a new chatMessage, we update the state and retransmit it
					// to all clients!
					this.updateChatState(castChMess);

					if (this.mainConsole != null && this.mainConsole instanceof pcConsole.Console) {
						this.mainConsole.printToConsole(castChMess.toString());
					}

					// obtain lock on current chatstate and send

					this.servTransponder.sendServerMessageToAll(this.chatState);

				}
			}

			// If the incoming ServerMessage is a ChatState - also print to console.
			// TODO: Handle this better in the future
			// A server shouldn't really be getting ServerMessages, but...we'll stay
			// flexible here.

			if (this.servTransponder.getNewSMFlag()) {

				try {

					inpSM = this.servTransponder.getLastSM();

					if (this.debugFlag == true) {
						System.out.println("pc-Server| Pulling last ServerMessage from Transponder...");
						System.out.println("pc-Server| Last ClientMessage reported from Transponder is: \n "
								+ inpSM.toString() + "\n");
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (inpSM instanceof ChatState) {
					ChatState castChState = (ChatState) inpSM;

					if (this.debugFlag == true) {
						System.out.println("pc-Server| Recieved ChatState object in listen() loop!");
					}
					if (this.mainConsole != null && this.mainConsole instanceof pcConsole.Console) {
						this.mainConsole.printToConsole(castChState.toString());
					}
				}

			}

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

		// Figure out what data structure you want to place that webPageLine string
		// into.

		try {
			// TODO: This is the ugliest way I could have found to parse this. This needs
			// review and improving!
			// The point is to actually read the rest of the line after we find the wordds
			// "competitive-rank-level"
			// currently: We just find the first and last reference, and pray that we get
			// what we want.

			while ((webPageLine = webInput.readLine()) != null) {

				if (webPageLine.contains("competitive-rank-level")) {

					int lineMarkerCRL = webPageLine.indexOf("competitive-rank-level");
					String SR = webPageLine.substring(lineMarkerCRL + 24, lineMarkerCRL + 28);
					Integer SRInt = Integer.valueOf(SR);

					// Always assign HighestSR found
					if (SRInt > playerHighestSR) {
						playerHighestSR = SRInt;
					}

					int lineMarkerNext = webPageLine.lastIndexOf("competitive-rank-level");
					SR = webPageLine.substring(lineMarkerNext + 24, lineMarkerNext + 28);
					SRInt = Integer.valueOf(SR);

					// Always assign HighestSR found
					if (SRInt > playerHighestSR) {
						playerHighestSR = SRInt;
					}

					System.out
							.println("Highest detected SR for player " + battleTag + "#" + btCode + " is: \n" + SRInt);
				}

				if (debugFlag == true) {
					System.out.println(webPageLine);
				}

			}

			// Check if platinum!
			if (playerHighestSR < 3000 && playerHighestSR >= 2500) {

				if (debugFlag == true) {
					System.out.println("Player " + battleTag + "#" + btCode + " is Platinum!");
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

	// Iterates through all the messages in the master chat log (AKA: Master Client
	// Message list)
	// Casts them to messages, if applicable, and adds them to the chatLog (provided
	// that the castMessage is not in the chatLog already!)

	private void updateChatState(ChatMessage message) {

		synchronized (this.chatState) {

			if (this.chatState != null && this.chatState instanceof ChatState) {
				this.chatState.addMessage(message);

				if (this.debugFlag == true) {
					System.out.println("pc-Server| Chat state updated! Added message: \n" + message.toString());
				}
			} else {
				if (this.debugFlag == true) {
					System.out.println("pc-Server| Failed to update chat state!");
				}
			}
		}

	}

	public static void main(String[] args) {

		Thread s = Thread.currentThread();
		s.setName("PC-Server Main");

		Server initServ = new Server();

		Scanner userInp = new Scanner(System.in);

		boolean stopFlag = false;

		Thread initThread = new Thread(initServ);
		initThread.setName("PC-Server");

		initThread.start();
		
		System.out.println("---CHAT---");
		System.out.println(initServ.getChatState());
		System.out.println("---ENDCHAT---");

		while (stopFlag == false) {
			System.out.println("Type 'x' to exit!");
			System.out.println("Type 'r' to refresh!");
			System.out.println("Enter message to send to clients:");

			String message = userInp.nextLine();

			if (message.compareTo("x") == 0) {

				stopFlag = true;

				initServ.servTransponder.stopAll();

				break;
			}

			switch (message) {
			case "r":
				System.out.println("---CHAT---");
				System.out.println(initServ.getChatState());
				System.out.println("---ENDCHAT---");
				break;
			case "x":
				System.out.println("Exiting PlatChat Server!");
				stopFlag = true;
				break;
			default:
				initServ.sendChatMessage(message);
			}


		}

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
		int ipPort = Integer.valueOf(inpScanner.nextLine());

		// prompt for player battletag
		System.out.println(prompt3);
		String battleTag = inpScanner.nextLine();

		// prompt for player battletag code
		System.out.println(prompt4);
		int battleTagCode = inpScanner.nextInt();

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

	// Retrieves last messages from Transponder and orders them by date/timestamp
	public String printLastChatLog() {
		return this.chatState.toString();
	}

	public ChatState getChatState() {
		return this.chatState;
	}

	public TransponderTCP getTransponder() {
		return this.servTransponder;
	}

	public String formatPrintLastMessages(ArrayList<ClientMessage<?>> inpList) {
		String result = "";
		for (ClientMessage<?> currMessage : inpList) {
			result += currMessage.toString() + "\n";
		}
		return result;

	}

	public void setConsole(pcConsole.Console inpConsole) {
		this.mainConsole = inpConsole;
	}

	public void setDebugFlag(boolean flag) {
		this.debugFlag = flag;
	}

	@Override
	public void run() {

		// If we (for whatever reason) have a thread assigned, new, and not started -
		// start the thread!
		if (this.transponderThread instanceof Thread && this.transponderThread.getState() == Thread.State.NEW) {
			this.transponderThread.start();
		}

		// listen() has an internal while-loop
		listen();

		// Assuming we are done listening, close the transponder object.
		this.servTransponder.closeServerIO();
	}

}
