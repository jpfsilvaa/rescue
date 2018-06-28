package newAgents;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import newAgents.AbstractAgent.who;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

public class PoliceOfficeAgent extends AbstractAgent<PoliceOffice> {
	
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
        	msgFinal = new String (msgRaw);
        	msgSplited = msgFinal.split(" ");
        }
        
        if (msgSplited != null) {
        	if (msgSplited.length > 1) {
		        if (channelMsgReceived == 1) {
		        	msgReceived = new MessageProtocol(channelMsgReceived, msgSplited[0],
		        			msgSplited[1].charAt(0), Integer.parseInt(msgSplited[2]), 
		        			new EntityID(Integer.parseInt(msgSplited[3])), Integer.parseInt(msgSplited[4]),
		        			Arrays.toString(subArray(msgSplited, 5, msgSplited.length)));
		        }
		        else if (channelMsgReceived == 2) {
		        	msgReceived = new MessageProtocol(channelMsgReceived, msgSplited[0],
		        			msgSplited[1].charAt(0), Integer.parseInt(msgSplited[2]), 
		        			new EntityID(Integer.parseInt(msgSplited[3])), 
		        			Arrays.toString(subArray(msgSplited, 4, msgSplited.length)));
		        }
		        
		        switch(messageFrom(channelMsgReceived, msgSplited)) {
		        	case AGENT:
		        		//System.out.println("(FS) Recebi a mensagem código " + messageSplited[4] + " do bombeiro que está no local " + messageSplited[4]);
			    		if (msgReceived.getCode() == 2) {
			    			String centralDestination = msgReceived.getDetails().split(", ")[0].substring(1);
			    			switch(centralDestination) {
			    				case "A":
			    					messages.add(new MessageProtocol(2, "C2C", 'A', time, this.getID(), msgReceived.getDetails()));
			    					break;
			    				case "F":
			    					messages.add(new MessageProtocol(2, "C2C", 'F', time, this.getID(), msgReceived.getDetails()));
			    					break;
			    				case "P":
			    					messages.add(new MessageProtocol(2, "C2C", 'A', time, this.getID(), msgReceived.getDetails()));
			    					break;
			    			}
			    		}
		        		break;
		        	case CENTRAL:
		        		System.out.println("(PO) Recebi a mensagem código " + msgReceived.getCode() + " de uma central");
		        		break;
		        	case NOTHING:
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
    
	private who messageFrom(int channelMsgReceived, String[] messageSplited) {
        who result = who.NOTHING;
        
		if(messageSplited != null) {
        	if (channelMsgReceived == 1) {
		        if (messageSplited[0].equals("A2C")) {
		        	if (messageSplited[1].equals("P")) {
		        		result = who.AGENT;
		        	}
		        }
        	}
        	else if (channelMsgReceived == 2) {
        		if (messageSplited[0].equals("C2C")) {
        			result = who.CENTRAL;
		        }
        	}
        }
        
        return result;
	}
	
	protected static<T> T[] subArray(T[] array, int begin, int end) { 
		return Arrays.copyOfRange(array, begin, end);
	}
}
