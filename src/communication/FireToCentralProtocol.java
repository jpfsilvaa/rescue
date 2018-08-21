package communication;

import rescuecore2.worldmodel.EntityID;

public class FireToCentralProtocol extends AbstractMessageProtocol{

	private String state;
	private int totalArea;
	private int fieryness;
	private int detailCodeTwo_1;
	private int detailCodeTwo_2;
	private EntityID senderPosition;
	private EntityID eventID;
	private char centerDestiny;
	
	public FireToCentralProtocol(int channel, String type, char agentChar, int time, 
			EntityID senderID, int code, String details) {
		super(channel, type, agentChar, time, senderID, code, details);
		
		String[] splitedDetails = details.split(" ");;
		switch(code) {
			case 0:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				break;
			case 1:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[2]));
				this.totalArea = Integer.parseInt(splitedDetails[3]);
				this.fieryness = Integer.parseInt(splitedDetails[4]);
				break;
			case 2:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[2]));
				this.centerDestiny = splitedDetails[3].charAt(0);
				this.detailCodeTwo_1 = Integer.parseInt(splitedDetails[4]);
				this.detailCodeTwo_2 = Integer.parseInt(splitedDetails[5]);
				break;
		}
	}
	
	public FireToCentralProtocol(int channel, String[] msgReceived) {
		super(channel, msgReceived); 
		switch(super.getCode()) {
		case 0:
			this.state = msgReceived[5];
			this.senderPosition = new EntityID(Integer.parseInt(msgReceived[6]));
			break;
		case 1:
			this.state = msgReceived[5];
			this.senderPosition = new EntityID(Integer.parseInt(msgReceived[6]));
			this.eventID = new EntityID(Integer.parseInt(msgReceived[7]));
			this.totalArea = Integer.parseInt(msgReceived[8]);
			this.fieryness = Integer.parseInt(msgReceived[9]);
			break;
		case 2:
			this.state = msgReceived[5];
			this.senderPosition = new EntityID(Integer.parseInt(msgReceived[6]));
			this.eventID = new EntityID(Integer.parseInt(msgReceived[7]));
			this.centerDestiny = msgReceived[8].charAt(0);
			this.detailCodeTwo_1 = Integer.parseInt(msgReceived[9]);
			this.detailCodeTwo_2 = Integer.parseInt(msgReceived[10]);
			break;
		}
	}

	public String getState() {
		return state;
	}

	public EntityID getSenderPosition() {
		return senderPosition;
	}

	public char getCenterDestiny() {
		return centerDestiny;
	}
	
	public int getTotalArea() {
		return totalArea;
	}

	public int getFieryness() {
		return fieryness;
	}

	public int getDetailCodeTwo_1() {
		return detailCodeTwo_1;
	}

	public int getDetailCodeTwo_2() {
		return detailCodeTwo_2;
	}
	
	public EntityID getEventID() {
		return this.eventID;
	}
	
}
