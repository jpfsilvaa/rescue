package communication;

import rescuecore2.worldmodel.EntityID;

public class A2C_Protocol extends AbstractMessageProtocol{

	private String state;
	private int detail_1;
	private int detail_2;
	private EntityID senderPosition;
	private EntityID eventID;
	private char centerDestiny;
	
	public A2C_Protocol(int channel, String type, char agentChar, int time, 
			EntityID senderID, int code, String details) {
		super(channel, type, agentChar, time, senderID, code, details);
		
		// TODO -> Se for usar essa subclasse do MEssagePRotocol, testa assim primeiro, depois, se tiver tempo, faz essa clase virar abstrata e cria subclasses por código que herdam essa classe aqui
		String[] splitedDetails = details.split(" ");;
		switch(code) {
			case 0:
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[0]));
				this.state = splitedDetails[1];
				break;
			case 1:
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[0]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.detail_1 = Integer.parseInt(splitedDetails[2]);
				this.detail_1 = Integer.parseInt(splitedDetails[3]);
				break;
			case 2:
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[0]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.centerDestiny = splitedDetails[2].charAt(0);
				this.detail_1 = Integer.parseInt(splitedDetails[3]);
				this.detail_1 = Integer.parseInt(splitedDetails[4]);
			
		}
		
	}

	private String getState() {
		return state;
	}

	private int getDetail_1() {
		return detail_1;
	}

	private int getDetail_2() {
		return detail_2;
	}

	private EntityID getSenderPosition() {
		return senderPosition;
	}

	private EntityID getEventID() {
		return eventID;
	}

	private char getCenterDestiny() {
		return centerDestiny;
	}
	
}
