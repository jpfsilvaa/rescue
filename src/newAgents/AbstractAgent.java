package newAgents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import communication.MessageConfirmation;
import communication.AbstractMessageProtocol;
import rescuecore2.messages.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;

public abstract class AbstractAgent<E extends StandardEntity> extends StandardAgent<E> {
	
	protected ArrayList<String> msgFinal = new ArrayList<>();
	protected AbstractMessageProtocol msgReceived;
	
	protected Random rnd;
	protected SampleSearch search;
	protected EntityID goal;
	protected ArrayList<AbstractMessageProtocol> messages = new ArrayList<>();
	protected List<Integer> blockadesPerceived = new ArrayList<>(); // amb e bomb
	protected List<Integer> civiliansPerceived = new ArrayList<>(); // bomb e pol
	protected List<Integer> buildingsInFirePerceived = new ArrayList<>(); // amb e pol
	protected MessageConfirmation confirmation;
	public enum Who{
		AGENT,
		CENTRAL,
		NOTHING
	};
	
	protected abstract HashMap <StandardEntityURN, List <EntityID>> percept(int time, ChangeSet perceptions);
	
	protected abstract void heardMessage(int time, Collection<Command> heard);
	
	protected abstract void deliberate(HashMap <StandardEntityURN, List <EntityID>> possibleGoals);
	
	protected abstract void act(int time);
	
}
