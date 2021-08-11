package pcConsole;

import java.util.Scanner;

import pcClient.Client;
import pcServer.Server;

public class Console {
	Server initServer = null;
	Client initClient = null;
	int mode = 0;

	Boolean stopFlag = false;
	Boolean debugFlag = false;

	public Console() {

		while (stopFlag == false) {

			if (mode == 1 && initServer != null) {
				if (debugFlag == true) {
					System.out.println("PlatChat| Server running!");
				}
			}

			if (mode == 2 && initClient != null) {
				if (debugFlag == true) {
					System.out.println("PlatChat| Client Running!");
				}
			}

			System.out.println("PlatChat| Type 'help' for assistance.");

			Scanner console = new Scanner(System.in);
			String userInp = console.nextLine();

			switch (userInp) {

			case "help":
				// TODO: Insert help dialogue here
				System.out.println("PlatChat| 'connect' to begin connecting" + " to a PlatChat server.");
				System.out.println("PlatChat| 'host' to begin hosting" + " a PlatChat server.");
				System.out.println("PlatChat| 'debug on' / 'debug off' to toggle debug messages");
				break;

			case "host":
				initServer = new Server();
				initServer.setConsole(this);
				Thread servThread = new Thread(initServer);
				servThread.start();
				mode = 1;
				break;

			case "connect":
				initClient = new Client();
				initClient.setConsole(this);
				Thread clientThread = new Thread(initClient);
				clientThread.start();
				mode = 2;
				break;
			case "debug on":
				debugFlag = true;
				break;
			case "debug off":
				debugFlag = false;
				break;
			case "getMasterList":
				if(this.mode == 1) {
					System.out.println(this.initServer.printLastChatLog().toString());
				}
				
				if(this.mode == 2) {
					System.out.println(this.initClient.debugGetLastClientMessage());
				}
				
				break;
			default:
				if (mode == 0) {
					if (debugFlag == true) {
						System.out.println("Must be hosting, or connected to, a PlatChat server!");
					}
				}
				if (mode == 1 && initServer != null) {
					if (debugFlag == true) {
						System.out.println("PlatChat| Server sending chat message!");
					}
					initServer.sendChatMessage(userInp);
				}

				if (mode == 2 && initClient != null) {
					if (debugFlag == true) {
						System.out.println("PlatChat| Client sending chat message!");
					}
					initClient.sendChatMessage(userInp);
				}
			}

		}

	}

	public static void main(String[] args) {
		Console mainCons = new Console();
	}
	

	public void printToConsole(String message) {
		System.out.println(message);
	}

}
