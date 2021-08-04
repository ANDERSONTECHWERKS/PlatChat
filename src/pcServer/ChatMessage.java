package pcServer;

import java.util.Date;

import transponderTCP.*;

public class ChatMessage extends ClientMessage<String>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChatMessage(String message) {
		super.setPayload(message);
	}
	
	@Override
	public String toString() {
		String result = "";
		
		result += "Message: " + super.getPayload().toString() +"\n";
		result += "Timestamp: " + super.getTimestamp() + "\n";
		return result;
	}
	
}
