package centers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import communication.AbstractMessageProtocol;
import communication.DummyProtocol;
import newAgents.AbstractAgent;
import newAgents.AbstractAgent.Who;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

public class FireStationAgent extends AbstractAgent<FireStation> {
	
	@Override
	protected HashMap<StandardEntityURN, List<EntityID>> percept(int time, ChangeSet perceptions) {
		return null;
	}

	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
		int channelMsgReceived = 0;
		EntityID whoSent = null;
		if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1, 2);
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	channelMsgReceived = msg.getChannel();
        	whoSent = msg.getAgentID();
        	byte[] msgRaw = msg.getContent();
        	// msgFinal = new String (msgRaw);
        	// msgSplited = msgFinal.split(" ");
        }
        
        // TODO -> TRATAR O ArrayList msgFinal e testar a confirmação de mensagem
        if (msgSplited != null) {
        	if (msgSplited.length > 1) {
		        switch(messageFrom(channelMsgReceived, msgSplited)) {
		        	case AGENT:
		        		msgReceived = new DummyProtocol(channelMsgReceived, msgSplited[0],
			        			msgSplited[1].charAt(0), Integer.parseInt(msgSplited[2]), 
			        			new EntityID(Integer.parseInt(msgSplited[3])), Integer.parseInt(msgSplited[4]),
			        			Arrays.toString(subArray(msgSplited, 5, msgSplited.length)));
		        		
		        		// System.out.println("+++++(FS) -> Recebi mensagem de código " + msgReceived.getCode());
			    		
		        		if (msgReceived.getCode() == 2) {
		        			System.out.println("details-> " + msgReceived.getDetails());
			    			String[] splitedDetails = msgReceived.getDetails().split(", ");
			    			String centralDestination = splitedDetails[2];
			    			messages.add(new DummyProtocol(2, "C2C", 'F', time, this.getID(), 
	    							3, (centralDestination + " " + splitedDetails[3] + " " + splitedDetails[4])));
	    					break;
			    		}
		        		msgSplited = null;
		        		break;
		        	case CENTRAL:
		        		msgReceived = new DummyProtocol(channelMsgReceived, msgSplited[0],
			        			msgSplited[1].charAt(0), Integer.parseInt(msgSplited[2]), 
			        			new EntityID(Integer.parseInt(msgSplited[3])), 3,
			        			Arrays.toString(subArray(msgSplited, 4, msgSplited.length)));
		        		System.out.println("(FS) Recebi a mensagem código " + msgReceived.getCode() + " de uma central");
		        		msgSplited = null;
		        		break;
		        	case NOTHING:
		        		msgSplited = null;
		        		break;
		        }
        	}
        }
	}

	@Override
	protected void deliberate(HashMap<StandardEntityURN, List<EntityID>> possibleGoals) {
		
	}

	@Override
	protected void act(int time) {
		
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		heardMessage(time, heard);
		
		if (messages.size() > 0) {
			sendSpeak(time, 2, messages.get(0).getEntireMessage().getBytes());
			messages.remove(0);
		}
	}

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_STATION,
                          StandardEntityURN.AMBULANCE_CENTRE,
                          StandardEntityURN.POLICE_OFFICE);
    }
    
	private Who messageFrom(int channelMsgReceived, String[] messageSplited) {
        Who result = Who.NOTHING;
        
		if(messageSplited != null) {
        	if (channelMsgReceived == 1) {
		        if (messageSplited[0].equals("A2C")) {
		        	if (messageSplited[1].equals("F")) {
		        		result = Who.AGENT;
		        	}
		        }
        	}
        	else if (channelMsgReceived == 2) {
        		if (messageSplited[0].equals("C2C")) {
        			System.out.println(Arrays.toString(messageSplited));
        			if (new EntityID(Integer.parseInt(messageSplited[3])) != this.getID())
        				if(messageSplited[5].equals("F"))
        					result = Who.CENTRAL;
		        }
        	}
        }
        
        return result;
	}
	
	protected static<T> T[] subArray(T[] array, int begin, int end) { 
		return Arrays.copyOfRange(array, begin, end);
	}
    
}