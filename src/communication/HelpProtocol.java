package communication;

import java.util.ArrayList;

import rescuecore2.worldmodel.EntityID;

public class HelpProtocol extends AbstractMessageProtocol {

	private EntityID agentDestiny;
	private EntityID placeToHelp;
	
	public HelpProtocol(int channel, char agentChar, 
			int time, EntityID myID, EntityID agentDestiny, EntityID placeToHelp) {
		super(channel, "C2A", agentChar, time, myID, 6, (agentDestiny.toString() + " " + placeToHelp.toString()));
		this.agentDestiny = agentDestiny;
		this.placeToHelp = placeToHelp;
	}
	
	public HelpProtocol(int channelReceived, String[] msgReceived) {
		super(channelReceived, msgReceived);
		this.agentDestiny = new EntityID(Integer.parseInt(msgReceived[5]));
		this.placeToHelp = new EntityID(Integer.parseInt(msgReceived[6]));
	}

	public EntityID getAgentDestiny() {
		return agentDestiny;
	}

	public EntityID getPlaceToHelp() {
		return placeToHelp;
	}
	
	public static boolean hasHelpMsgToSend(ArrayList<AbstractMessageProtocol> messages) {
		boolean hasHelpMsg = false;

		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof HelpProtocol)
				hasHelpMsg = true;

		return hasHelpMsg;
	}
	
	public static HelpProtocol getHelpMsgFromList(ArrayList<AbstractMessageProtocol> messages) {
		HelpProtocol result = null;
		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof HelpProtocol)
				result = (HelpProtocol) mp;
		return result;
	}

}
