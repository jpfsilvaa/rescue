package newAgents;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

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
	protected HashMap<StandardEntityURN, List<EntityID>> percept(ChangeSet perceptions) {
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
        	msgFinal = new String (msgRaw);
        	messageSplited = msgFinal.split(" ");
        }
        
        if(messageSplited != null) {
	        if (messageSplited[0].equals("A2C")) {
	        	if (messageSplited[1].equals("A")) {
	        		System.out.println("(AC) Recebi a mensagem código " + messageSplited[3] + " da ambulancia que está no local " + messageSplited[4]);
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
	}

	
    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_STATION,
                          StandardEntityURN.AMBULANCE_CENTRE,
                          StandardEntityURN.POLICE_OFFICE);
    }
}
