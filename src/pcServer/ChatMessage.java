package pcServer;

import java.util.Date;

public class ChatMessage {
	String message = "";
	String username = "";
	Date date = null;
	
	ChatMessage(String message, String username){
		this.message = message;
		this.username = username;
		date = new Date();
	}
	
}
