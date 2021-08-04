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


public class Tests extends TestCase{
	static {
		InetSocketAddress lb6969 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),6969);
		InetSocketAddress lb7000 =  new InetSocketAddress(InetAddress.getLoopbackAddress(),7000);
		
		try {
			Socket clientSock = new Socket(lb6969.getAddress(),6969,lb7000.getAddress(),7000);
			ServerSocket servSock = new ServerSocket(6969,1,lb6969.getAddress());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPlatChecker() {
		System.out.println("Is theJames plat? " + Server.isPlayerPlat("thejames", 1611, false));
	}
	
	@Test
	public void testServerCreation() {
		
		Server testServ = new Server();
		testServ.run();
	}
}
