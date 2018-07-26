package communication;

import rescuecore2.worldmodel.EntityID;

public class DummyProtocol extends AbstractMessageProtocol{

	public DummyProtocol(int channel, String type, char agentChar, int time, EntityID senderID, int code,
			String details) {
		super(channel, type, agentChar, time, senderID, code, details);
	}

}
