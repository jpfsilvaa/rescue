package communication;

import rescuecore2.worldmodel.EntityID;

/**
 * @author jsilva
 * <p>Classe que faz o tratamento das mensagens do agente policial para sua central e vice-versa.
 */
public class PoliceToCentralProtocol extends AbstractMessageProtocol {
	private String state;
	private EntityID blockadePosition;
	private int blockadeRepairCost;
	private int detailCodeTwo_1;
	private int detailCodeTwo_2;
	private EntityID senderPosition;
	private EntityID eventID;
	private char centerDestiny;
	
	public PoliceToCentralProtocol(int channel, String type, char agentChar, int time, 
			EntityID senderID, int code, String details) {
		super(channel, type, agentChar, time, senderID, code, details);
		
		String[] splitedDetails = details.split(" ");;
		switch(Protocol.get(code)) {
			case AGENT_STATUS:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.eventID = new EntityID(0);
				this.blockadeRepairCost = 0;
				this.blockadePosition = new EntityID(0);
				break;
			case AGENT_EVENT:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[2]));
				this.blockadeRepairCost = Integer.parseInt(splitedDetails[3]);
				this.blockadePosition = new EntityID(Integer.parseInt(splitedDetails[4]));
				break;
			case AGENT_EXTERN_EVENT:
				this.state = splitedDetails[0];
				this.senderPosition = new EntityID(Integer.parseInt(splitedDetails[1]));
				this.eventID = new EntityID(Integer.parseInt(splitedDetails[2]));
				this.centerDestiny = splitedDetails[3].charAt(0);
				this.detailCodeTwo_1 = Integer.parseInt(splitedDetails[4]);
				this.detailCodeTwo_2 = Integer.parseInt(splitedDetails[5]);
				break;
		}
	}
	
	public PoliceToCentralProtocol(int channel, String[] msgReceived) {
		super(channel, msgReceived); 
		switch(Protocol.get(super.getCode())) {
		case AGENT_STATUS:
			this.state = msgReceived[5];
			this.senderPosition = new EntityID(Integer.parseInt(msgReceived[6]));
			this.eventID = new EntityID(0);
			this.blockadeRepairCost = 0;
			this.blockadePosition = new EntityID(0);
			break;
		case AGENT_EVENT:
			this.state = msgReceived[5];
			this.senderPosition = new EntityID(Integer.parseInt(msgReceived[6]));
			this.eventID = new EntityID(Integer.parseInt(msgReceived[7]));
			this.blockadeRepairCost = Integer.parseInt(msgReceived[8]);
			this.blockadePosition = new EntityID(Integer.parseInt(msgReceived[9]));
			break;
		case AGENT_EXTERN_EVENT:
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

	public EntityID getBlockadePosition() {
		return blockadePosition;
	}

	public int getBlockadeRepairCost() {
		return blockadeRepairCost;
	}

	public int getDetailCodeTwo_1() {
		return detailCodeTwo_1;
	}

	public int getDetailCodeTwo_2() {
		return detailCodeTwo_2;
	}

	public EntityID getSenderPosition() {
		return senderPosition;
	}

	public EntityID getEventID() {
		return eventID;
	}

	public char getCenterDestiny() {
		return centerDestiny;
	}
	
		
	
}
