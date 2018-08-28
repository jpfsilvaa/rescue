package communication;

import rescuecore2.worldmodel.EntityID;

/**
 * @author jsilva
 * <p>Classe que faz o tratamento das mensagens de Central para Central.
 */
public class CentralToCentralProtocol extends AbstractMessageProtocol {
	private final static int CHANNEL = 2;
	private final static String TYPE = "C2C";
	private final static int CODE = 3;
	private char centerDestiny;
	private EntityID eventID;
	private EntityID eventPosition;
	private int detail_1;
	private int detail_2;	
	
	public CentralToCentralProtocol(String[] msgReceived) {
		super(CHANNEL, msgReceived); 
		this.centerDestiny = msgReceived[5].charAt(0);
		this.eventID = new EntityID(Integer.parseInt(msgReceived[6]));
		this.eventPosition = new EntityID(Integer.parseInt(msgReceived[7]));		
		this.detail_1 = Integer.parseInt(msgReceived[8]);
		this.detail_2 = Integer.parseInt(msgReceived[9]);
	}

	public CentralToCentralProtocol(char center, int time, EntityID senderId, String details) {
		super(CHANNEL, TYPE, center, time, senderId, CODE, details);
		String detailsSplitted[] = details.split(" ");
		this.centerDestiny = detailsSplitted[0].charAt(0);
		this.eventID = new EntityID(Integer.parseInt(detailsSplitted[1]));
		this.eventPosition = new EntityID(Integer.parseInt(detailsSplitted[2]));		
		this.detail_1 = Integer.parseInt(detailsSplitted[3]);
		this.detail_2 = Integer.parseInt(detailsSplitted[4]);
	}

	public char getCenterDestiny() {
		return centerDestiny;
	}

	public EntityID getEventID() {
		return eventID;
	}

	public EntityID getEventPosition() {
		return eventPosition;
	}

	public int getDetail_1() {
		return detail_1;
	}

	public int getDetail_2() {
		return detail_2;
	}
	
}
