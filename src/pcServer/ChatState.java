package pcServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import transponderTCP.ClientMessage;
import transponderTCP.MessageDateComparatorCM;
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
	
	public synchronized void addMessage(ChatMessage inpMessage) {

		if(!this.getPayload().contains(inpMessage)) {
			this.getPayload().add(inpMessage);
		}

		this.sortChatLogByDate();
	}
	
	public ArrayList<ChatMessage> getChatLog(){
		return this.getPayload();
	}
	
	private void sortChatLogByDate() {
		MessageDateComparatorCM dateComparator = new MessageDateComparatorCM();

		this.getPayload().sort(dateComparator);
	}
	
	public String printSortedByComparator(Comparator<ChatMessage> comp) {
		
		ArrayList<ChatMessage> unsortCL = this.getPayload();

		unsortCL.sort(comp);
		
		return unsortCL.toString();
		
	}
	
	@Override
	public synchronized String toString() {
		MessageDateComparatorCM dateComparator = new MessageDateComparatorCM();
		
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
