package newAgents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jfree.util.Rotation;

import communication.AbstractMessageProtocol;
import communication.DummyProtocol;
import communication.HelpProtocol;
import communication.MessageConfirmation;
import communication.Protocol;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.messages.MessageComponent;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;

public class PoliceAgent extends AbstractAgent<PoliceForce>{
	public PoliceAgent() {
		rnd = new Random(System.currentTimeMillis());
	}	
	
	private enum State{
		READY,
    	PATROL,
    	REMOVING_BLOCKADE,
    	MOVING
    }
	private State state = State.READY;
	private int distance;
	private static final String DISTANCE_KEY = "clear.repair.distance";
	private PoliceForce me;
	private EntityID currentBlockade;
	private boolean recipientHasReceived = false;
	private int channelMsgReceived = 0; 
	
    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
        System.out.println("FORÇA POLICIAL CONECTADA!");
        distance = config.getIntValue(DISTANCE_KEY);
        me = (PoliceForce) me();
        search = new SampleSearch(model);
    }
	
     /**
      * A percepção de bloqueio por meio do changed não estava sendo muito eficiente, pois o agente
      * precisava criar um caminho até o bloqueio da sua percepção, e este caminho estava dando sempre nulo,
      * e este método "getTargetBLockade()" está funcionando, como funcionava em versões anteriores  
      * do agente.
      */
	@Override
	protected HashMap<StandardEntityURN, List<EntityID>> percept(int time, ChangeSet perceptions) {
		List<EntityID> blocks = new ArrayList<EntityID>();
		List<EntityID> roadsToPatrol = new ArrayList<EntityID>();
		
		Blockade target = getTargetBlockade();
		if(target != null) {
			blocks.add(target.getID());
		}
		
		for(EntityID changed : perceptions.getChangedEntities()) {
			switch(model.getEntity(changed).getStandardURN()) {
				case CIVILIAN:
					Human civilian = (Human) model.getEntity(changed);
					if (!civiliansPerceived.contains(changed.getValue())) { 
						if(civilian.isBuriednessDefined() && civilian.getBuriedness() > 1) {
							messages.add(new DummyProtocol(1, "A2C", 'P', time, me.getID(), 2, 
									(state + " " + me.getPosition() + " " + civilian.getID() + " A " + civilian.getBuriedness() 
									+ " " + civilian.getHP())));
						}
						civiliansPerceived.add(changed.getValue());
					}
				break;
				case ROAD:
					roadsToPatrol.add(changed);
				break;
				case BUILDING:
					Building buildingPerceived = (Building) model.getEntity(changed);
					if (!buildingsInFirePerceived.contains(changed.getValue())) {
						if (buildingPerceived.isOnFire() && buildingPerceived.getFieryness() > 1) {
							messages.add(new DummyProtocol(1, "A2C", 'P', time, me.getID(), 2, 
									(state + " " + me.getPosition() + " " + buildingPerceived.getID() + " F " 
									+ buildingPerceived.getTotalArea() + " " + buildingPerceived.getFieryness())));
						}
						buildingsInFirePerceived.add(changed.getValue());
					}
				break;
			}
		}
		
		HashMap <StandardEntityURN, List <EntityID>> selectedPerceptions = new HashMap <StandardEntityURN, List <EntityID>>();
		selectedPerceptions.put(StandardEntityURN.BLOCKADE, blocks);
		selectedPerceptions.put(StandardEntityURN.ROAD, roadsToPatrol);
		return selectedPerceptions;
	}

	@Override
	protected void heardMessage(int time, Collection<Command> heard) {
    	if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
            sendSubscribe(time, 1);
    	
        for (Command next : heard) {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	channelMsgReceived = msg.getChannel();
        	byte[] msgRaw = msg.getContent();
        	msgFinal.add(new String (msgRaw));
        }

        handleMessage(time);
	}

	/**
	 * Se o agente não estiver no estado READY ou PATROL, então ele está em algum estado
	 * que é sua principal ação, então o primeiro if já retorna da função, mas se não,
	 * ele verifica se há bloqueios a serem limpos, se não há, então ele verifica 
	 * as estradas, nesta ordem.
	 * @param possibleGoals é o HashMap que guarda os possíveis objetivos que o agente irá cumprir, 
	 * e foi preenchido pelo método de percepção do agente.
	 */
	@Override
	protected void deliberate(HashMap<StandardEntityURN, List<EntityID>> possibleGoals) {
		if(state != State.READY && state != State.PATROL)
			return;
		
		if(possibleGoals.get(StandardEntityURN.BLOCKADE).size() > 0) {
			setGoal(StandardEntityURN.BLOCKADE, possibleGoals, State.REMOVING_BLOCKADE);
			return;
		}
		
		if(possibleGoals.get(StandardEntityURN.ROAD).size() > 0) {
			setGoal(StandardEntityURN.ROAD, possibleGoals, State.PATROL);
			return;
		}	
	}

	/**
	 * O estado REMOVING_BLOCKADE utiliza a variável global
	 * "currentBlockade" que é definida no método deliberate() (por meio do método setGoal)
	 * e assim que é feito um "sendClear() com esse bloqueio atual, ele recebe null, para um próximo
	 * bloqueio ser inserido nesta mesma variável e ser limpo também. <br>
	 * O estado PATROL faz uma patrulha nas vias que o agente encontra em seu método de percepção, mas 
	 * só faz esta patrulha se não houver bloqueio a ser retirado.
	 */
	@Override
	protected void act(int time) {
		List <EntityID> path = new ArrayList<EntityID>();
		switch(state) {
			case REMOVING_BLOCKADE:
				if(currentBlockade == null)
					state = State.READY;
				else {
					final Blockade currBlock = (Blockade) model.getEntity(currentBlockade);
					messages.add(new DummyProtocol(1, "A2C", 'P', time, me.getID(), 1, 
							(state + " " + me.getPosition() + " " + currBlock.getID() + " " 
							+ currBlock.getRepairCost() + " " + currBlock.getPosition())));
					sendClear(time, currentBlockade);
					currentBlockade = null;
				}
				break;
			case PATROL:
				path = search.breadthFirstSearch(location().getID(), goal);
				if(me.getPosition().getValue() == goal.getValue())
					state = State.READY;
				else
					sendMove(time, path);
				break;
			case MOVING:
				path = search.breadthFirstSearch(location().getID(), goal);
				if (path != null) {
					if(me.getPosition().getValue() == goal.getValue()) {
						state = State.READY;
					}
					else
						sendMove(time, path);
				}
				else {
					state = State.READY;
				}
				break;
				
		}
		
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		sendMessages(time);
		heardMessage(time, heard);
		HashMap <StandardEntityURN, List <EntityID>> goals = percept(time, changed);
		deliberate(goals);
		act(time);		
		System.out.println();
	}
	
	/**
	 * <p>Esse método define qual vai ser o EntityID do objetivo do agente:
	 * @param urn é o URN de qual tipo de EntityID, no model atual, para saber qual objetivo será traçado
	 * @param hm é o HashMap que contém os EntityIDs dos objetivos, obtidos pela percepção do agente
	 * @param s é o estado que vai ser definido, ao ser definido o objetivo
	 */
	private void setGoal(StandardEntityURN urn, HashMap<StandardEntityURN, List<EntityID>> hm, State s) {
		if(s.equals(State.REMOVING_BLOCKADE)) {
			currentBlockade = hm.get(urn).get(rnd.nextInt(hm.get(urn).size()));
			state = s;
		}
		else {
			goal = hm.get(urn).get(rnd.nextInt(hm.get(urn).size()));
			state = s;
		}
	}
	
    private Blockade getTargetBlockade() {
        Logger.debug("Looking for target blockade");
        Area location = (Area)location();
        Logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, distance);
        if (result != null) {
            return result;
        }
        Logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private Blockade getTargetBlockade(Area area, int maxDistance) {
        if (area == null || !area.isBlockadesDefined())
            return null;
        List<EntityID> ids = area.getBlockades();
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            if (maxDistance < 0 || d < maxDistance)
                return b;
        }
        return null;
    }

    private int findDistanceTo(Blockade b, int x, int y) {
        List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y);
        for (Line2D next : lines) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            if (d < best)
                best = d;

        }
        return (int)best;
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
	        					goal = hpReceived.getPlaceToHelp();
	        					state = State.MOVING;
	        				}
	        				break;
	        		}
	        		
	        	}
	        }
        }
    }

	@Override
	public void sendMessages(int time) {
		msgFinal.clear();
		if (messages.size() == 0) // Só mando código zero se não há código 1 ou 2 a ser enviado ainda.
			messages.add(new DummyProtocol(1, "A2C", 'P', time, me.getID(), 
					0, state + " " + me.getPosition().toString()));
		
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
}
