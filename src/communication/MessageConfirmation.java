package communication;

import java.util.ArrayList;

import rescuecore2.worldmodel.EntityID;


public class MessageConfirmation extends AbstractMessageProtocol{
	private EntityID destiny;
	
	public MessageConfirmation(int channel, String type, char agentChar, int time, 
			EntityID myID, int code, String details) {
		super(channel, type, agentChar, time, myID, code, (details + " RECEIVED"));
		this.destiny = new EntityID(Integer.parseInt(details));
	}
	
	public EntityID getDestiny() {
		return destiny;
	}
	
	public static boolean hasConfirmationToSend(ArrayList<AbstractMessageProtocol> messages) {
		boolean hasConfirmationMsg = false;

		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof MessageConfirmation)
				hasConfirmationMsg = true;

		return hasConfirmationMsg;
	}
	
	public static MessageConfirmation getConfirmationMsgFromList(ArrayList<AbstractMessageProtocol> messages) {
		MessageConfirmation result = null;
		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof MessageConfirmation)
				result = (MessageConfirmation) mp;
		return result;
	}
	
}
