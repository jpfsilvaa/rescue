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

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;

public class FireAgent extends AbstractAgent<FireBrigade>{

    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";
    private int maxWater;
    private int maxPower;
	private FireBrigade me;	
	private enum State 
	{
		READY,
		RECHARGING,
		MOVING,
		EXTINGUISHING
	}
	private State state = State.READY;
	
	
	public FireAgent() {
		rnd = new Random(System.currentTimeMillis());
	}
	
	protected void postConnect() {
		super.postConnect();
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        me = (FireBrigade) me();
        search = new SampleSearch(model);
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}
	
	
	@Override
	protected HashMap <StandardEntityURN, List <EntityID>> percept(int time, ChangeSet perceptions) {
		List <EntityID> roads = new ArrayList<EntityID>();
		List <EntityID> buildings = new ArrayList<EntityID>();
		EntityID myPosition = this.location().getID();
		
		for(EntityID changed : perceptions.getChangedEntities()) {
			switch(model.getEntity(changed).getStandardURN()) {
				case CIVILIAN:
					Human civilian = (Human) model.getEntity(changed);
					if (!civiliansPerceived.contains(changed.getValue())) {
						if (civilian.isBuriednessDefined() && civilian.getBuriedness() > 1) {
							System.out.println("PERCEBI CIVIL!!!!!!!-B");
							messages.add(new MessageProtocol(1, "A2C", 'A', time, me.getID(), 2, 
									(me.getPosition() + " " + civilian.getID() + " " +
									"A " + civilian.getBuriedness() + " " +
									civilian.getHP() + civilian.getStamina())));
						}
						civiliansPerceived.add(changed.getValue());
					}
				break;
				case ROAD:
					if(myPosition.getValue() != changed.getValue())
						roads.add(changed);
					break;
				case BUILDING:
					if(myPosition.getValue() != changed.getValue()) {
						Building building = (Building) model.getEntity(changed);
						if(building.getFieryness() > 0 && building.getFieryness() < 4) {
							buildings.add(changed);
						}
					}
					break;
				case BLOCKADE:
					Blockade b = (Blockade) model.getEntity(changed);
					// System.out.println("changed -> " + Arrays.toString(b.getApexes()));
					List<Integer> currentBlockade = Arrays.stream(b.getApexes()).boxed().collect(Collectors.toList());
					/*
					 * // verifica se não é o mesmo bloqueio comparando os vertices 
					 * (se houver mesmo vértice, então fazparte do mesmo) e evita o envio
					 * de muitas mensagens para o mesmo bloqueio.
					 */
					if (Collections.disjoint(blockadesPerceived, currentBlockade)) {
						blockadesPerceived.addAll(Arrays.stream(b.getApexes()).boxed().collect(Collectors.toList()));
						// System.out.println("last" + Arrays.toString(b.getApexes()));
						messages.add(new MessageProtocol(1, "A2C", 'F', time, me.getID(), 2, 
								("P " + me.getPosition().toString() + " " + b.getRepairCost())));
					}
					break;
			}
		}
		
		HashMap <StandardEntityURN, List <EntityID>> selectedPerceptions = new HashMap <StandardEntityURN, List <EntityID>>();
		selectedPerceptions.put(StandardEntityURN.ROAD, roads);
		selectedPerceptions.put(StandardEntityURN.BUILDING, buildings);	
		
		return selectedPerceptions;
	}
	
	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
    	if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 2);
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	//System.out.println("URN-->" + msg.getURN());
        	byte[] msgRaw = msg.getContent();
        	msgFinal = new String (msgRaw);
        	msgSplited = msgFinal.split(" ");
        }
        //if(messageResult != null)
        	//System.out.println("->(F) MESSAGE RECEIVED: " + messageResult.toString());
    	/*if(messageResult != null) {
        	for(int i = 0; i < messageResult.length; i++)
        		System.out.print("MESSAGE(SPLIT) RECEIVED: " + messageResult[i] + "\t");
        	System.out.println();
        	if(messageResult.length == 1) {
        		if(messageResult[0] == "Help" || messageResult[0] == "Ouch") {
        			System.out.println(this.getID() + "CIVILIAN ASKING FOR HELP AROUND HERE!!");
        			// CRIAR UM PROTOCOLO NAS PRÓXIMAS FASES
        		}
        	}
        }*/
	}
	
	@Override
	protected void deliberate(HashMap <StandardEntityURN, List <EntityID>> possibleGoals) {
		if(me.getWater() == 0)
			state = State.RECHARGING;
		
		if(state != State.READY)
			return;
		
		if(possibleGoals.get(StandardEntityURN.BUILDING).size() > 0) {	
			setGoal(StandardEntityURN.BUILDING, possibleGoals, State.EXTINGUISHING);
			return;
		}
		
		if(possibleGoals.get(StandardEntityURN.ROAD).size() > 0) {
			setGoal(StandardEntityURN.ROAD, possibleGoals, State.MOVING);
			return;
		}	
	}
	
	
	@Override
	protected void act(int time) {
		switch(state) {
			case EXTINGUISHING:
				Building buildingGoal = (Building) model.getEntity(goal);
				if (!buildingsInFirePerceived.contains(goal.getValue())) {
					messages.add(new MessageProtocol(1, "A2C", 'F', time, me.getID(), 1, 
							(me.getPosition() + " " + buildingGoal.getID() + " " +
							buildingGoal.getFloors() + " " + buildingGoal.getFieryness())));
					buildingsInFirePerceived.add(goal.getValue());
				}
				//System.out.println("\tEXTINGUISHING!");
				//System.out.println("TANK: " + me.getWater());
				//System.out.println("BUILDING FIERYNESS: "+ buildingGoal.getFieryness());
				sendExtinguish(time, goal, maxPower);
				if(buildingGoal.getFieryness() >= 4)
					state = State.READY;
				break;
			case MOVING:
				if(model.getEntity(goal) instanceof Building) {
					Building building = (Building)model.getEntity(goal);
					List<EntityID> neighbours = building.getNeighbours();
					if(building.getFieryness() > 4) {
						state = State.READY;
						break;
					}
					for(EntityID neighbour : neighbours) {
						if(model.getEntity(neighbour) instanceof Road) {
							List<EntityID> backToBuilding = search.breadthFirstSearch(this.location().getID(), neighbour);
							if(backToBuilding == null) {
								System.out.println("NULL PATH TO GO BACK!");
								state = State.READY;
								break;
							}else {
								sendMove(time, backToBuilding);
								if(this.location().getID().getValue() == neighbour.getValue())
									state = State.EXTINGUISHING;
							}
							break;
						}
					}
				}else {
					List <EntityID> path = search.breadthFirstSearch(this.location().getID(), goal);
					
					if(path == null) 
						System.out.println("NO PATH TO " + goal.getValue());
					else if(this.location().getID().getValue() == goal.getValue()) {
						//System.out.println("ARRIVED TO " + goal.getValue());
						state = State.READY;
					}else {
						//System.out.println("PATH TO GOAL \n");
						//for(EntityID node : path)
							//System.out.println("ID : "+node.getValue()+"\tTYPE: "+model.getEntity(node).getURN());
						sendMove(time, path);
					}
				}
				break;
			case RECHARGING:
				Collection<StandardEntity> refuges = model.getEntitiesOfType(StandardEntityURN.REFUGE);
				List<EntityID> RefugePath = null;
        		for(StandardEntity next: refuges) {
	        		RefugePath = search.breadthFirstSearch(this.location().getID(), next.getID());
	        		if(RefugePath != null)
		        		sendMove(time, RefugePath);
	        		if(me.getWater() == maxWater)
	        			state = State.MOVING;
        		}
				break;
		}
			
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		if (messages.size() == 0) // Só mando código zero se não há código 1 ou 2 a ser enviado ainda.
			messages.add(new MessageProtocol(1, "A2C", 'F', time, me.getID(), 0, time +
					" " + me.getPosition().toString() + " " + state)); // Código 0 ao Centro

		// MessageProtocol m = MessageProtocol.setFirstMessagesOnQueue(messages); // PEGA A PRIMEIRA MENSAGEM POR PRIORIDADE E RETORNA AO OBJETO m
		messages = MessageProtocol.setFirstMessagesOnQueue(messages);
		if (messages.size() > 0) {
			// sendSpeak(time, messages.get(0).getChannel(), (messages.get(0).getEntireMessage()).getBytes());
			messages.remove(0);
		}
		
		heardMessage(time, heard);
		//System.out.println("(F)STATE---> " + state);
		HashMap <StandardEntityURN, List <EntityID>> goals = percept(time, changed);
		deliberate(goals);
		act(time);
		System.out.println();
	}

	private void setGoal(StandardEntityURN urn, HashMap<StandardEntityURN, List<EntityID>> hm, State s) {
		goal = hm.get(urn).get(rnd.nextInt(hm.get(urn).size()));
		//System.out.println("FOUND A NEW GOAL AT " + goal.getValue());
		state = s;
	}

}
