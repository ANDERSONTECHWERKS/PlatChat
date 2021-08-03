package pcServer;

import java.io.Serializable;
import java.util.concurrent.PriorityBlockingQueue;

public class ChatState implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PriorityBlockingQueue<ChatMessage> messages = new PriorityBlockingQueue<ChatMessage>();
}
