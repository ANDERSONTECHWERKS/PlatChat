package Tests;
import pcServer.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;
import pcClient.Client;


public class Tests extends TestCase{
	static {

	}
	
	@Test
	public void testPlatChecker() {
		System.out.println("Is theJames plat? " + Server.isPlayerPlat("thejames", 1611, false));
	}
	
	@Test
	public void testServerCreation() {
		
		// Begin standard socket init block
		InetSocketAddress lb6969 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),6969);
		InetSocketAddress lb7000 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),7000);
		ServerSocket servSock = null;
		
		try {
			servSock = new ServerSocket(6969,1,lb6969.getAddress());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// End standard socket init block
		
		Server testServ = new Server(servSock,true);
		
		Thread testServThread = new Thread(testServ);
		
		testServThread.start();
		testServ.sendChatMessage("Rad 1");
		testServ.sendChatMessage("Rad 2");
		testServ.sendChatMessage("Rad 3");

	}
	
	@Test
	public void testClientCreation() {
		
		// Begin standard socket init block
		InetSocketAddress lb6969 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),6969);
		InetSocketAddress lb7000 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),7000);
		Socket cliSock = null;
		
		try {
			cliSock = new Socket();
			cliSock.bind(lb7000);
			cliSock.connect(lb6969);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// End standard socket init block
		
		Client testClient = new Client(cliSock,true);
		
		Thread testCliThread = new Thread(testClient);
		
		testCliThread.start();
		testClient.sendChatMessage("Test1");
		testClient.sendChatMessage("Test2");
		testClient.sendChatMessage("Test3");
	}
	
	@Test
	public void testServerCreationClientConnect() {
		
		// Begin standard socket init block
		InetSocketAddress lb6969 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),6969);
		InetSocketAddress lb7000 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),7000);
		ServerSocket servSock = null;
		Socket clientSock = null;
		
		try {
			servSock = new ServerSocket(6969,1,lb6969.getAddress());
			clientSock = new Socket(lb6969.getAddress(),6969,lb7000.getAddress(),7000);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// End standard socket init block
		
		Server testServ = new Server(servSock,true);
		testServ.setDebugFlag(true);
		Thread servThread = new Thread(testServ);
		
		servThread.start();
		
		Client testClient = new Client(clientSock,true);
		testClient.setDebugFlag(true);
		Thread clientThread = new Thread(testClient);
		
		clientThread.start();
		
		testClient.sendChatMessage("Radical Bread");
		testServ.sendChatMessage("Butthead Prime");
		
		ChatState servCS = testServ.getChatState();
		ChatState clientCS = testClient.getChatState();

		System.out.println(testClient.getTransponder().getStatus());
		System.out.println("*--Last testServ ChatLog returned--*\n"+servCS.toString());
		System.out.println("*--Last testClient ChatLog returned--*\n"+clientCS.toString());

	}
	
	@Test
	public void testConsole() {
		
		// Begin standard socket init block
		InetSocketAddress lb6969 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),6969);
		InetSocketAddress lb7000 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),7000);
		ServerSocket servSock = null;
		Socket clientSock = null;
		
		try {
			servSock = new ServerSocket(6969,1,lb6969.getAddress());
			clientSock = new Socket(lb6969.getAddress(),6969,lb7000.getAddress(),7000);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// End standard socket init block
		
	}
}
