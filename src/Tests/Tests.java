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
		testServ.run();
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
		Thread servThread = new Thread(testServ);
		
		servThread.start();
		
		Client testClient = new Client(clientSock,true);
		Thread clientThread = new Thread(testClient);
		
		clientThread.start();
		
		testClient.sendMessage("Radical Bread");
		
		testServ.getLastMessages().toString();
		testClient.debugGetLastChatMessage();
		
	}
}
