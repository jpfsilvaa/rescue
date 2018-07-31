package centers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import communication.DummyProtocol;
import communication.MessageConfirmation;
import communication.AbstractMessageProtocol;
import newAgents.AbstractAgent;
import newAgents.AbstractAgent.Who;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

public class AmbulanceCentreAgent extends AbstractAgent<AmbulanceCentre> {	
	@Override
	protected HashMap<StandardEntityURN, List<EntityID>> percept(int time, ChangeSet perceptions) {
		return null;
	}

	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
		int channelMsgReceived = 0;
		EntityID whoSent = null;
		if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1, 2); // não consegui inserir mais de dois canais
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	channelMsgReceived = msg.getChannel();
        	whoSent = msg.getAgentID();
        	byte[] msgRaw = msg.getContent();
        	msgFinal.add(new String (msgRaw));
        }
 
        //TODO -> Depois que houver um tramamento de mensagens mesmo, fazer isso aqui virar um metodo do AbstractAgent, pra evitar repetição em todos centros desse cógido
        for (String message : msgFinal) {
        	String[] msgSplited = message.split(" ");
	        if (msgSplited != null) {
	        	if (msgSplited.length > 1) {		        
			        switch(messageFrom(channelMsgReceived, msgSplited)) {
				    	case AGENT:
				    		msgReceived = new DummyProtocol(channelMsgReceived, msgSplited);
				    		
				    		if (msgReceived.getCode() == 2) {
				    			String[] splitedDetails = msgReceived.getDetails().split(", ");
				    			String centralDestination = splitedDetails[3];
				    			messages.add(new DummyProtocol(2, "C2C", 'A', time, this.getID(), 
		    							3, (centralDestination + " " + splitedDetails[4] + " " + splitedDetails[5])));
				    		}
				    		
				    		msgSplited = null;
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), msgReceived.getType(), 'A', time, this.getID(), 5, msgReceived.getSenderID().toString()));
				    		break;
				    	case CENTRAL:
				    		msgReceived = new DummyProtocol(channelMsgReceived, 3, msgSplited);
				    		// TODO -> Pegar os detalhes da mensagem percebida e designar um agente para resolver o evento
				    		msgSplited = null;
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), msgReceived.getType(), 'A', time, this.getID(), 5, msgReceived.getSenderID().toString()));
				    		break;
				    	case NOTHING:
				    		msgSplited = null;
				    		break;
			        }
			        
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
		msgFinal.clear();
		heardMessage(time, heard);
		
		// TODO -> Fazer com que a central receba e reconheça a confirmação de uma mensagem que ela recebeu também.
		
		if (messages.size() > 0) {
			if (MessageConfirmation.hasConfirmationToSend(messages)) {
				MessageConfirmation mc = MessageConfirmation.getConfirmation(messages);
				sendSpeak(time, mc.getChannel(), mc.getEntireMessage().getBytes());
				messages.remove(mc);
			}
			if (messages.size() > 0) {
				sendSpeak(time, 2, messages.get(0).getEntireMessage().getBytes());
				messages.remove(0);
			}
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
		        	if (messageSplited[1].equals("A")) {
		        		result = Who.AGENT;
		        	}
		        }
        	}
        	else if (channelMsgReceived == 2) {
        		if (messageSplited[0].equals("C2C")) {
        			if (new EntityID(Integer.parseInt(messageSplited[3])) != this.getID())
        				if(messageSplited[5].equals("A"))
        					result = Who.CENTRAL;
		        }
        	}
        }
        
        return result;
	}

}
