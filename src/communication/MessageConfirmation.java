package communication;

import java.util.ArrayList;

import rescuecore2.worldmodel.EntityID;


public class MessageConfirmation extends AbstractMessageProtocol{
	private EntityID destiny;
	
	public MessageConfirmation(int channel, String type, char agentChar, int time, 
			EntityID myID, int code, EntityID destiny) {
		super(channel, type, agentChar, time, myID, code, (destiny + " RECEIVED"));
		this.destiny = destiny;
	}
	
	public EntityID getDestiny() {
		return destiny;
	}
	
	// TODO -> Testar o funcionamento desse método
	public static boolean hasConfirmationToSend(ArrayList<AbstractMessageProtocol> messages) {
		boolean hasConfirmationMsg = false;

		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof MessageConfirmation)
				hasConfirmationMsg = true;

		return hasConfirmationMsg;
	}
	
	public static MessageConfirmation getConfirmation(ArrayList<AbstractMessageProtocol> messages) {
		MessageConfirmation result = null;
		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof MessageConfirmation)
				result = (MessageConfirmation) mp;
		return result;
	}
	
}
