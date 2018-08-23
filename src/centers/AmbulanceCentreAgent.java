package centers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.DummyProtocol;
import communication.HelpProtocol;
import communication.MessageConfirmation;
import communication.PoliceToCentralProtocol;
import communication.Protocol;
import communication.AbstractMessageProtocol;
import communication.AmbToCentralProtocol;
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
	
	private HashMap<EntityID, AmbToCentralProtocol> agentsState = new HashMap<>();
	
	@Override
	protected HashMap<StandardEntityURN, List<EntityID>> percept(int time, ChangeSet perceptions) {
		return null;
	}

	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
		int channelMsgReceived = 0;
		if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1, 2);
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	channelMsgReceived = msg.getChannel();
        	byte[] msgRaw = msg.getContent();
        	msgFinal.add(new String (msgRaw));
        }
 
        for (String message : msgFinal) {
        	String[] msgSplited = message.split(" ");
	        if (msgSplited != null) {
	        	if (msgSplited.length > 1) {		        
			        switch(messageFrom(channelMsgReceived, msgSplited)) {
				    	case AGENT:
				    		msgReceived = new AmbToCentralProtocol(channelMsgReceived, msgSplited);
				    		AmbToCentralProtocol aMsgReceived = (AmbToCentralProtocol) msgReceived;
				    		
				    		updateAgentsState(aMsgReceived);
				    		System.out.println(aMsgReceived.getState());
				    		
				    		if (Protocol.get(aMsgReceived.getCode()) == Protocol.AGENT_EXTERN_EVENT) {
				    			messages.add(new AmbToCentralProtocol(2, "C2C", 'A', time, this.getID(), 
		    							3, (aMsgReceived.getCenterDestiny() + " " + aMsgReceived.getDetailCodeTwo_1() + " " + aMsgReceived.getDetailCodeTwo_2())));
				    		}
				    		else if (Protocol.get(msgReceived.getCode()) == Protocol.AGENT_EVENT) {
				    			if(aMsgReceived.getCivilBuriedness() >= 20) {
				    				System.out.println("####CIVIL COM MAIS DE 50 DE BURIEDNESS");
				    				getHelp(time, aMsgReceived);
				    			}
				    		}
				    		
				    		msgSplited = null;
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), "C2A", 'A', time, this.getID(), 5, msgReceived.getSenderID().toString()));
				    		break;
				    	case CENTRAL:
				    		msgReceived = new DummyProtocol(channelMsgReceived, 3, msgSplited);
				    		// TODO -> Pegar os detalhes da mensagem percebida e designar um agente para resolver o evento
				    		msgSplited = null;
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), "C2A", 'A', time, this.getID(), 5, msgReceived.getSenderID().toString()));
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
		sendMessages(time);
		heardMessage(time, heard);
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

	/**
	 * Método que verifica no hashMap de estados dos agentes 
	 * bombeiros para definir a quem será solicitada ajuda, 
	 * e assim defini um HelpProtocol a ser enviado.
	 */
	private void getHelp(int time, AmbToCentralProtocol aMsgReceived) {
		for (Map.Entry<EntityID, AmbToCentralProtocol> entry : agentsState.entrySet()) {
			EntityID agent = entry.getKey();
			if (agentsState.get(agent).getState().equals("READY") 
					|| agentsState.get(agent).getState().equals("PATROL")
					|| agentsState.get(agent).getState().equals("BUILDING_SEARCH")) {
				messages.add(new HelpProtocol(1, 'A', time, this.getID(), 
						agent, aMsgReceived.getSenderPosition()));
				break;
			}
			else {
				/* Caso todos os agentes policiais já estejam ocupados, é verifficado
				 * qual deles está reparando um bloqueio de custo menor que o pedido de ajuda.
				 */
				if (agentsState.get(agent).getCivilHP() < aMsgReceived.getCivilHP()) {
					messages.add(new HelpProtocol(1, 'P', time, this.getID(), 
							agent, aMsgReceived.getSenderPosition()));
					break;
				}
			}
		}		
	}
	
	/**
	 * Adiciona o state de cada agente a um hashMap pra central 
	 * saber quem está disponível caso necessite enviar ajuda
	 * a algum outro bombeiro.
	 */
	private void updateAgentsState(AmbToCentralProtocol aMsgReceived) {
		agentsState.put(aMsgReceived.getSenderID(), aMsgReceived);
	}
	
	@Override
	public void sendMessages(int time) {
		msgFinal.clear();
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

}
