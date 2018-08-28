package newAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import communication.AbstractMessageProtocol;
import communication.AmbToCentralProtocol;
import communication.DummyProtocol;
import communication.HelpProtocol;
import communication.MessageConfirmation;
import communication.Protocol;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;

public class AmbulanceAgent extends AbstractAgent<AmbulanceTeam>{

	public AmbulanceAgent() {
		rnd = new Random(System.currentTimeMillis());
	}	
	private enum State {
		READY,
		RESCUING,
		PATROL,
		UNLOADING,
		BUILDING_SEARCH, 
		MOVING
	}
	private State state = State.READY;
	private AmbulanceTeam me = (AmbulanceTeam) me();
	private List <EntityID> exploredBuildings = new ArrayList<EntityID>();
	private boolean recipientHasReceived = false;
	private int channelMsgReceived = 0;
	private boolean isOnHelpAsk = false;
	
	protected void postConnect() {
		super.postConnect();
        me = (AmbulanceTeam) me();
        search = new SampleSearch(model);
	}
	
	@Override
	protected HashMap <StandardEntityURN, List <EntityID>> percept(int time, ChangeSet perceptions) {
		List <EntityID> roads = new ArrayList <EntityID>();
		List <EntityID> buildings = new ArrayList <EntityID>();
		List <EntityID> possibleRescue_civilian = new ArrayList <EntityID>();
		EntityID myPosition = this.location().getID();
		
		for(EntityID changed : perceptions.getChangedEntities()) {
			switch(model.getEntity(changed).getStandardURN()) {
				case CIVILIAN:
					Human c = (Human) model.getEntity(changed);
					if (!getRefuges().contains(c.getPosition(model)))
						possibleRescue_civilian.add(changed);
				break;
				case ROAD:
					if(myPosition.getValue() != changed.getValue())
						roads.add(changed);
					break;
				case BUILDING:
					Building buildingPerceived = (Building) model.getEntity(changed);
					if (buildingPerceived.isOnFire() && buildingPerceived.getFieryness() > 1) {
						if (!buildingsInFirePerceived.contains(changed.getValue())) {
							messages.add(new AmbToCentralProtocol(1, "A2C", 'A', time, me.getID(), 2, 
								(state + " " + me.getPosition() + " " + buildingPerceived.getID() + 
								" F " + buildingPerceived.getTotalArea() + " " + buildingPerceived.getFieryness())));
						}
						buildingsInFirePerceived.add(changed.getValue());
					}						
					else {
						if(myPosition.getValue() != changed.getValue()) {
							if(wasExplored(changed)) {
								//state = State.READY;
								break;
							} 
							else {
								exploredBuildings.add(changed);
								Building building = (Building) model.getEntity(changed);
								if(building.getFieryness() < 1)
									buildings.add(changed);
								break;
							}
						}
					}
					break;
				case BLOCKADE:
					Blockade b = (Blockade) model.getEntity(changed);
					// System.out.println("changed -> " + Arrays.toString(b.getApexes()));
					List<Integer> currentBlockade = Arrays.stream(b.getApexes()).boxed().collect(Collectors.toList());
					/*
					 * verifica se não é o mesmo bloqueio comparando os vertices 
					 * (se houver mesmo vértice, então faz parte do mesmo) e evita o envio
					 * de muitas mensagens sobre o mesmo bloqueio.
					 */
					if (Collections.disjoint(blockadesPerceived, currentBlockade)) {
						blockadesPerceived.addAll(Arrays.stream(b.getApexes()).boxed().collect(Collectors.toList()));
						messages.add(new AmbToCentralProtocol(1, "A2C", 'A', time, me.getID(), 2, 
								(state + " " + me.getPosition() + " " + b.getID() + " P " + b.getRepairCost() + " " + b.getPosition())));
					}
					break;
			}			
		}
	
		HashMap <StandardEntityURN, List <EntityID>> selectedPerceptions = new HashMap <StandardEntityURN, List <EntityID>>();
		selectedPerceptions.put(StandardEntityURN.ROAD, roads);
		selectedPerceptions.put(StandardEntityURN.BUILDING, buildings);
		selectedPerceptions.put(StandardEntityURN.CIVILIAN, possibleRescue_civilian);
		
		return selectedPerceptions;
	}
	
	
	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
		if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1);
		
    	EntityID who = null;
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	who = msg.getAgentID();
        	channelMsgReceived = msg.getChannel();
        	byte[] msgRaw = msg.getContent();
        	msgFinal.add(new String (msgRaw));
        }
        
        handleMessage(time);

    }

	@Override
	protected void deliberate(HashMap <StandardEntityURN, List <EntityID>> possibleGoals) {
		if(state == State.RESCUING || state == State.MOVING 
				|| state == State.UNLOADING || state == State.BUILDING_SEARCH) {
			return;
		}
		else {
			if(possibleGoals.get(StandardEntityURN.CIVILIAN).size() > 0) {
				setGoal(StandardEntityURN.CIVILIAN, possibleGoals, State.MOVING);
				return;
			}
	
			if(possibleGoals.get(StandardEntityURN.BUILDING).size() > 0) {
				setGoal(StandardEntityURN.BUILDING, possibleGoals, State.BUILDING_SEARCH);
				return;
			}
	
			if(possibleGoals.get(StandardEntityURN.ROAD).size() > 0) {
				setGoal(StandardEntityURN.ROAD, possibleGoals, State.PATROL);
				return;
			}	
			
			// CASO ENTRE EM ALGUM PRÉDIO E PARE DE PERCEBER TUDO, ADICIONAR ESTE ENTITYID SOMENTE PARA SAIR SO LUGAR
			goal = new EntityID(314);
			state = State.PATROL;
			return;
		}
	}

	@Override
	protected void act(int time) {
		List <EntityID> path = new ArrayList<EntityID>();
		switch(state) {
			case BUILDING_SEARCH:
				movState(goal, path, State.PATROL, time);
				break;
			case PATROL:
				movState(goal, path, State.READY, time);
				break;
			case MOVING:
				if (isOnHelpAsk) {
					movState(goal, path, State.READY, time);
				}
				else
					movState(goal, path, State.RESCUING, time);
				break;
			case RESCUING:
				Human civilian = (Human) model.getEntity(goal);
				if (!civiliansPerceived.contains(goal.getValue())) { // TOMAR CUIDADO AQUI POR QUE ELE PODE PERCEBER, DEPOIS DESISTIR, AÍ NÃO ENTRA AQUI MAIS
					if(civilian.isBuriednessDefined() && civilian.getBuriedness() > 1) {
						messages.add(new AmbToCentralProtocol(1, "A2C", 'A', time, me.getID(), 1, 
									(state + " " + me.getPosition() + " " + civilian.getID() +
										" " + civilian.getBuriedness() + " " + civilian.getHP())));
					}
					civiliansPerceived.add(goal.getValue());
				}
				if(civilian.getBuriedness() == 0) {
					sendLoad(time, goal);
					state = State.UNLOADING;
				}
				else {
					System.out.println("Rescue Buriedness-->" + civilian.getBuriedness());
					sendRescue(time, goal);
					
				}
				break;
			case UNLOADING:
				Collection<StandardEntity> refuges = model.getEntitiesOfType(StandardEntityURN.REFUGE);
        		for(StandardEntity next: refuges) {
	        		path = search.breadthFirstSearch(me().getPosition(), next.getID());
	        		if(path != null) {
		        		sendMove(time, path);
		        		if(location() instanceof Refuge) {
		        			//sendUnload(time); // ficou no think
		        			state = State.READY;
		        			break;
		        		}		        		
	        		}
        		}
        		break;
		}
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		sendMessages(time);
		
		if(someoneOnBoard() && location() instanceof Refuge) {
			System.out.println("(A) UNLOADING");
			sendUnload(time);
		}
		
		heardMessage(time, heard);
		HashMap <StandardEntityURN, List <EntityID>> goals = percept(time, changed);
		deliberate(goals);
		act(time);
		System.out.println();
	}
	
	@Override
	protected EnumSet getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

	
	/**
	 * <p>Esse método define qual vai ser o EntityID do objetivo do agente:
	 * @param urn é o URN de qual tipo de EntityID, no model atual, para saber qual objetivo será traçado
	 * @param hm é o HashMap que contém os EntityIDs dos objetivos, obtidos pela percepção do agente
	 * @param s é o estado que vai ser definido, ao ser definido o objetivo
	 */
	private void setGoal(StandardEntityURN urn, HashMap<StandardEntityURN, List<EntityID>> hm, State s) {
		goal = hm.get(urn).get(rnd.nextInt(hm.get(urn).size()));
		//System.out.println("FOUND A NEW GOAL "+ urn.toString() +  " AT " + goal + " myPosition: " + me.getPosition() 
		//+ " myBuriedness: " + me.getBuriedness());
		state = s;
	}
	
	/**
	 * <p>Método que modifica o estado do agente e define seu objetivo.
	 * @param goal Objetivo do agente
	 * @param path caminho para o agente atingir seu objetivo
	 * @param nxtState estado para o qual o agente será definido
	 * @time Ciclo do simulador
	 */
	private void movState(EntityID goal, List<EntityID> path, State nxtState, int time) {
		if(model.getEntity(goal) instanceof Human) {
			Human c = (Human) model.getEntity(goal);
			if(!(model.getEntity(c.getPosition()) instanceof Refuge))
				path = search.breadthFirstSearch(me.getPosition(),c.getPosition());
			else {
				state = State.READY;
				return;
			}
			
			if(me.getPosition().getValue() == c.getPosition().getValue()) {
				state = nxtState;
				return;
			}
			else {
				if(path != null) {
					if (path.size() >= 1) {
						sendMove(time, path);
					}
				}
				else {
					state = State.READY;
					return;
				}
			}
		}
		else
			path = search.breadthFirstSearch(me.getPosition(), goal);
		
		
		if(me.getPosition().getValue() == goal.getValue()) {
			isOnHelpAsk = false;
			state = nxtState;
			return;
		}
		else {
			if(path.size() >= 1) {
				sendMove(time, path);
			}
			else {
				state = State.READY;
				return;
			}
		}
	}
	
	/**
	 * <p>Método que verifica se um prédio ja foi visitado de acordo com seu entity
	 * @param changed entnti do prédio percebido
	 * @return true se o prédio ja foi visitado
	 */
	private boolean wasExplored(EntityID changed) {
		if(exploredBuildings.contains(changed))
			return true;
		else
			return false;
	}

	/**
	 * Método que verifica se já existe um civil dentro da ambulancia
	 * @return true se já existe um civil dentro da ambulancia
	 */
    private boolean someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void sendMessages(int time) {
		msgFinal.clear();
		if (messages.size() == 0) // Só mando código zero se não há código 1 ou 2 a ser enviado ainda.
			messages.add(new AmbToCentralProtocol(1, "A2C", 'A', time, me.getID(), 
					0, state + " " + me.getPosition().toString())); // Código 0 ao Centro
		
		messages = AbstractMessageProtocol.setFirstMessagesOnQueue(messages);
		if (messages.size() > 0) {
			if (!recipientHasReceived) {
				sendSpeak(time, 1, (messages.get(0).getEntireMessage()).getBytes());
			}
			else {
				recipientHasReceived  = false;
				messages.remove(0);
			}
		}
	}
	
	@Override
	public void handleMessage(int time) {
		for (String msgReceived : msgFinal) {
	        String[] msgSplited = msgReceived.split(" ");
        	if (msgSplited != null) {
	        	if (msgSplited.length > 1) {
	        		
	        		int code = Integer.parseInt(msgSplited[4]);
	        		switch(Protocol.get(code)) {
	        			case CONFIRMATION_MSG:
	        				MessageConfirmation confirmation = new MessageConfirmation(channelMsgReceived, msgSplited[0], 
	        						msgSplited[1].charAt(0), Integer.parseInt(msgSplited[2]), 
	        						new EntityID(Integer.parseInt(msgSplited[3])), code, 
	        						msgSplited[5]);
	        				if (confirmation.getDestiny().getValue() == me.getID().getValue()) {
	        					recipientHasReceived = true;
	        				}
	        				msgSplited = null;
	        				break;
	        			case HELP_PROTOCOL:
	        				HelpProtocol hpReceived = new HelpProtocol(channelMsgReceived, msgSplited);
	        				if (hpReceived.getAgentDestiny().getValue() == me.getID().getValue()) {
	        					if (!someoneOnBoard()) {
		        					goal = hpReceived.getPlaceToHelp();
		        					isOnHelpAsk = true;
		        					state = State.MOVING;
	        					}
	        						
	        				}
	        				break;
	        		}
	        		
	        	}
	        }
        }
	}
	
}
