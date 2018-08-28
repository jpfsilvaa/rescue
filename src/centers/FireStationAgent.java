package centers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.AbstractMessageProtocol;
import communication.CentralToCentralProtocol;
import communication.DummyProtocol;
import communication.FireToCentralProtocol;
import communication.HelpProtocol;
import communication.MessageConfirmation;
import communication.PoliceToCentralProtocol;
import communication.Protocol;
import newAgents.AbstractAgent;
import newAgents.AbstractAgent.Who;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * @author jsilva
 * 
 * <p>Classe que representa o centro de comunicação dos agentes do tipo FireBrigade.
 *
 */
public class FireStationAgent extends AbstractAgent<FireStation> {
	
	private HashMap<EntityID, FireToCentralProtocol> agentsState = new HashMap<>();
	public int channelMsgReceived = 0;	
	
	@Override
	protected HashMap<StandardEntityURN, List<EntityID>> percept(int time, ChangeSet perceptions) {
		return null;
	}

	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
		if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1, 2);
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	channelMsgReceived = msg.getChannel();
        	byte[] msgRaw = msg.getContent();
        	msgFinal.add(new String (msgRaw));
        }
        
        handleMessage(time);
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
    
    /**
     * Método que, de acordo com a mensagem recebida, 
     * retorna quem mandou esta mesma mensagem.
     * quem mandou a mensagem. 
     * @param channelMsgReceived canal pelo qual a mensagem foi recebida
     * @param messageSplited mensagem da qual será identificado quem mandou
     * @return Enum do tipo {@link Who} identificando
     */
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
	 * Adiciona o estado de cada agente a um hashMap pra central 
	 * saber quem está disponível caso necessite enviar ajuda
	 * a algum outro bombeiro.
	 * @param aMsgReceived Representa a mensagem enviada pelo agente que essa central coordena.
	 */
	private void updateAgentsState(FireToCentralProtocol fMsgReceived) {
		agentsState.put(fMsgReceived.getSenderID(), fMsgReceived);
	}
	
	/**
	 * <p>Método que verifica no hashMap de estados dos agentes 
	 * que essa central coordena para definir a quem será solicitada ajuda, 
	 * e assim defini um {@link HelpProtocol} a ser enviado.
	 * <p>Caso todos os agentes já estejam ocupados, é verificado
	 * qual deles está resolvendo um evento que é menos prioritário que esse pedido de ajuda.
	 * @param time Ciclo do simulador que é utilizado na instancia de um {@link HelpProtocol} 
	 * @param aMsgReceived Objeto que representa a mensagem recebida
	 */
	private void getHelp(int time, FireToCentralProtocol fMsgReceived) {
		for (Map.Entry<EntityID, FireToCentralProtocol> entry : agentsState.entrySet()) {
			EntityID agent = entry.getKey();
			if (agentsState.get(agent).getState().equals("READY") || 
					agentsState.get(agent).getState().equals("MOVING")) {
				messages.add(new HelpProtocol(1, 'F', time, this.getID(), 
						agent, fMsgReceived.getEventID()));
				break;
			}
			else {
				if (agentsState.get(agent).getTotalArea() < fMsgReceived.getTotalArea()) {
					messages.add(new HelpProtocol(1, 'F', time, this.getID(), 
							agent, fMsgReceived.getEventID()));
					break;
				}
			}
		}		
	}
	
	/**
	 * <p>Método similar ao getHelp que, de acordo com o 
	 * pedido de ajuda vindo de uma outra central, determina 
	 * qual agente deve se responsabilizar pelo evento.
	 * @param time Ciclo do simulador
	 * @param cMsgReceived Objeto que representa a mensagem recebida de uma outra central
	 */
	private void sendHelp(int time, CentralToCentralProtocol cMsgReceived) {
		for (Map.Entry<EntityID, FireToCentralProtocol> entry : agentsState.entrySet()) {
			EntityID agent = entry.getKey();
			if (agentsState.get(agent).getState().equals("READY") 
					|| agentsState.get(agent).getState().equals("MOVING")) {
				messages.add(new HelpProtocol(1, 'F', time, this.getID(), 
						agent, cMsgReceived.getEventPosition()));
				break;
			}
			else {
				if (agentsState.get(agent).getTotalArea() < cMsgReceived.getDetail_1()) {
					messages.add(new HelpProtocol(1, 'F', time, this.getID(), 
							agent, cMsgReceived.getEventPosition()));
					break;
				}
			}
		}		
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

	@Override
	public void handleMessage(int time) {
		for (String message : msgFinal) {
        	String[] msgSplited = message.split(" ");
	        if (msgSplited != null) {
	        	if (msgSplited.length > 1) {		        
			        switch(messageFrom(channelMsgReceived, msgSplited)) {
				    	case AGENT:				    		
				    		msgReceived = new FireToCentralProtocol(channelMsgReceived, msgSplited);
				    		FireToCentralProtocol fMsgReceived = (FireToCentralProtocol) msgReceived;
				    		
				    		updateAgentsState(fMsgReceived);
				    		
				    		if (Protocol.get(fMsgReceived.getCode()) == Protocol.AGENT_EXTERN_EVENT) {
				    			messages.add(new CentralToCentralProtocol('F', time, this.getID(), 
		    							(fMsgReceived.getCenterDestiny() + " " + fMsgReceived.getEventID() +
		    									" " + fMsgReceived.getSenderPosition() + " " + fMsgReceived.getDetailCodeTwo_1() +
		    									" " + fMsgReceived.getDetailCodeTwo_2())));
				    		}
				    		else if (Protocol.get(msgReceived.getCode()) == Protocol.AGENT_EVENT) {
				    			if(fMsgReceived.getTotalArea() > 50 && fMsgReceived.getFieryness() < 3) {
				    				getHelp(time, fMsgReceived);
				    			}
				    		}
				    		
				    		msgSplited = null;
				    		
				    		// ADICIONANDO A CONFIRMAÇÃO DE MENSAGEM NA FILA
					        messages.add(new MessageConfirmation(msgReceived.getChannel(), "C2A", 'F', time, this.getID(), 5, msgReceived.getSenderID().toString()));
				    		break;
				    	case CENTRAL:
				    		msgReceived = new CentralToCentralProtocol(msgSplited);
				    		CentralToCentralProtocol cMsgReceived = (CentralToCentralProtocol) msgReceived;
				    		
				    		sendHelp(time, cMsgReceived);
				    		
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
	
}
