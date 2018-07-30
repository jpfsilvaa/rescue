package communication;

import java.util.Arrays;

import newAgents.AbstractAgent;
import rescuecore2.worldmodel.EntityID;

public class DummyProtocol extends AbstractMessageProtocol{

	public DummyProtocol(int channel, String type, char agentChar, int time, EntityID senderID, int code,
			String details) {
		super(channel, type, agentChar, time, senderID, code, details);
	}
	
	public DummyProtocol(int channel, String[] msgReceived) {
		super(channel, msgReceived);
	}
	
	public DummyProtocol(int channel, int code, String[] msgReceived) {
		super(channel, code, msgReceived);
	}

}
