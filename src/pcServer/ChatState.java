package pcServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import transponderTCP.ClientMessage;
import transponderTCP.MessageDateComparator;
import transponderTCP.ServerMessage;

public class ChatState extends ServerMessage<ArrayList<ChatMessage>> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	Date startDate = null;
	
	public ChatState() {
		ArrayList<ChatMessage> initLog = new ArrayList<ChatMessage>();
		
		this.setPayload(initLog);
		
		startDate = new Date();
	}
	
	public ChatState(String initMessage) {
		startDate = new Date();
		
		ChatMessage initCM = new ChatMessage(initMessage);
		ArrayList<ChatMessage> initLog = new ArrayList<ChatMessage>();
		
		initLog.add(initCM);
		
		this.setPayload(initLog);
		
	}
	
	public void addMessage(ChatMessage inpMessage) {
		MessageDateComparator dateComparator = new MessageDateComparator();
		
		this.getPayload().add(inpMessage);
		this.getPayload().sort(dateComparator);
	}
	
	public ArrayList<ChatMessage> getChatLog(){
		return this.getPayload();
	}
	
	private void sortChatLog(Comparator<ChatMessage> comp) {
		this.getPayload().sort(comp);
	}
	
	@Override
	public String toString() {
		MessageDateComparator dateComparator = new MessageDateComparator();
		
		// Sort the chatLog right before presentation
		
		this.getPayload().sort(dateComparator);
		
		String result = "";
		
		for(ChatMessage currMessage : this.getPayload()) {
			result += currMessage.toString() + "\n";
		}
		
		return result;
	}

	@Override
	public int compareTo(ServerMessage<ArrayList<ChatMessage>> arg0) {
		
		if(this.startDate.before(arg0.getTimestamp())) {
			return -1;
		}
		
		if(this.startDate.after(arg0.getTimestamp())) {
			return 1;
		}
		
		return 0;
	}
}
