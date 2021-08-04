package pcServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import transponderTCP.ClientMessage;
import transponderTCP.MessageDateComparator;
import transponderTCP.ServerMessage;

public class ChatState extends ServerMessage<ChatState> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ChatMessage> chatLog = new ArrayList<ChatMessage>();
	
	Date startDate = null;
	
	public ChatState() {
		startDate = new Date();
		
	}
	
	public void addMessage(ChatMessage inpMessage) {
		MessageDateComparator dateComparator = new MessageDateComparator();
		
		this.chatLog.add(inpMessage);
		this.chatLog.sort(dateComparator);
	}
}
