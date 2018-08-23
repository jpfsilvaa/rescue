package communication;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.lang.Integer;

public enum Protocol {
	AGENT_STATUS(0),
	AGENT_EVENT(1),
	AGENT_EXTERN_EVENT(2),
	CENTRAL_TO_CENTRAL(3),
	HELP_PROTOCOL(4),
	CONFIRMATION_MSG(5);
	
	private static final HashMap<Integer,Protocol> lookup = new HashMap<Integer, Protocol>();
	
	static {
        for(Protocol p : EnumSet.allOf(Protocol.class))
             lookup.put(p.getCode(), p);
	}
	
	private int code;
	
	Protocol(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public static Protocol get(int code) {
		return lookup.get(code);
	}

}
