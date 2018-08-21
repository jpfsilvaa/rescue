package centers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.AbstractMessageProtocol;
import communication.DummyProtocol;
import communication.FireToCentralProtocol;
import communication.HelpProtocol;
import communication.MessageConfirmation;
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
	
	private HashMap<EntityID, String> agentsState = new HashMap<>();
	
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
        	msgFinal.add(new String (msgRaw));
        }
        
        for (String message : msgFinal) {
        	String[] msgSplited = message.split(" ");
	        if (msgSplited != null) {
	        	if (msgSplited.length > 1) {		        
			        switch(messageFrom(channelMsgReceived, msgSplited)) {
				    	case AGENT:				    		
				    		msgReceived = new FireToCentralProtocol(channelMsgReceived, msgSplited);
				    		FireToCentralProtocol fMsgReceived = (FireToCentralProtocol) msgReceived;
				    		
				    		updateAgentsState(fMsgReceived);
				    		
				    		if (msgReceived.getCode() == 2) {
				    			messages.add(new DummyProtocol(2, "C2C", 'F', time, this.getID(), 
		    							3, (fMsgReceived.getCenterDestiny() + " " + fMsgReceived.getDetailCodeTwo_1() + " " + fMsgReceived.getDetailCodeTwo_2())));
				    		}
				    		else if (msgReceived.getCode() == 1) {
				    			if(fMsgReceived.getTotalArea() > 50 && fMsgReceived.getFieryness() < 3) {
				    				getHelp(time, fMsgReceived);
				    			}
				    		}
				    		
				    		msgSplited = null;
				    		
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), "C2A", 'F', time, this.getID(), 5, msgReceived.getSenderID().toString()));
				    		break;
				    	case CENTRAL:
				    		msgReceived = new DummyProtocol(channelMsgReceived, 3, msgSplited);
				    		// TODO -> Pegar os detalhes da mensagem percebida e designar um agente para resolver o evento
				    		msgSplited = null;
				    		
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), msgReceived.getType(), 'F', time, this.getID(), 5, msgReceived.getSenderID().toString()));
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
		
		System.out.println(agentsState.toString());
		
		if (messages.size() > 0) {
			if (HelpProtocol.hasHelpMsgToSend(messages)) {
				HelpProtocol hp = HelpProtocol.getHelpMsgFromList(messages);
				sendSpeak(time, hp.getChannel(), hp.getEntireMessage().getBytes());
				messages.remove(hp);
			}
			if (MessageConfirmation.hasConfirmationToSend(messages)) {
				MessageConfirmation mc = MessageConfirmation.getConfirmationMsgFromList(messages);
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
		        	if (messageSplited[1].equals("F")) {
		        		result = Who.AGENT;
		        	}
		        }
        	}
        	else if (channelMsgReceived == 2) {
        		if (messageSplited[0].equals("C2C")) {
        			if (new EntityID(Integer.parseInt(messageSplited[3])) != this.getID())
        				if(messageSplited[5].equals("F"))
        					result = Who.CENTRAL;
		        }
        	}
        }
        
        return result;
	}
    
	/**
	 * Adiciona o state de cada agente a um hashMap pra central 
	 * saber quem está disponível caso necessite enviar ajuda
	 * a algum outro bombeiro.
	 */
	private void updateAgentsState(FireToCentralProtocol fMsgReceived) {
		agentsState.put(fMsgReceived.getSenderID(), fMsgReceived.getState());
	}
	
	/**
	 * Método que verifica no hashMap de estados dos agentes 
	 * bombeiros para definir a quem será solicitada ajuda, 
	 * e assim defini um HelpProtocol a ser enviado.
	 */
	private void getHelp(int time, FireToCentralProtocol fMsgReceived) {
		for (Map.Entry<EntityID, String> entry : agentsState.entrySet()) {
			EntityID agent = entry.getKey();
			if (agentsState.get(agent).equals("READY") || 
					agentsState.get(agent).equals("MOVING")) {
				messages.add(new HelpProtocol(1, 'F', time, this.getID(), 
						agent, fMsgReceived.getEventID()));
				break;
			}
		}		
	}
	
}
