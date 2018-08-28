package newAgents;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	/**
	 * <p>Método que obtém cada tipo de entidade da percepção que o agente teve em cada 
	 * ciclo de acordo com o que o simulador fornece. 
	 * @param time Ciclo da simulador
	 * @param perceptions {@link ChangeSet} vindo do método think que contém todas as entidades que o agente percebeu
	 * @return HashMap que separa os tipos de entidades percebidas pelo agente.
	 */
	protected abstract HashMap <StandardEntityURN, List <EntityID>> percept(int time, ChangeSet perceptions);
	
	/**
	 * Método que analisa as mensagens recebidas pelo agente em cada ciclo.
	 * @param time Ciclo do simulador
	 * @param heard Coleção de mensagens recebidas pelo agente
	 */
	protected abstract void heardMessage(int time, Collection<Command> heard);
	
	/**
	 * Método que define as ações a serem tomadas de acordo com 
	 * as percepções do agente, estas que são fornecidas pelo método percept
	 * @param possibleGoals HashMap que contém as percepções do agente separadas por tipo de entidade
	 */
	protected abstract void deliberate(HashMap <StandardEntityURN, List <EntityID>> possibleGoals);
	
	/**
	 * Método que, de acordo com as definições feitas no método deliberate, 
	 * faz o agente agir com os seus objetivos determinados
	 * @param time Ciclo do simulador
	 */
	protected abstract void act(int time);
	
	/**
	 * Método que obtém somente partes de um array, utilizado no tratamento de mensagens.
	 * @param array Array completo a ser dividido
	 * @param begin Define o começo do corte do array
	 * @param end Define o fim do corte do Array
	 * @return Array dividido de acordo com os parâmetros
	 */
	public static<T> T[] subArray(T[] array, int begin, int end) { 
		return Arrays.copyOfRange(array, begin, end);
	}
	
	/**
	 * Método que, de acordo com os objetivos definidos em outros métodos 
	 * pelo agente, faz o envio das mensagens.
	 * @param time Ciclo do simulador
	 */
	public abstract void sendMessages(int time);
	
	/**
	 * Método que faz o tratamento das mensagens recebidas pelo agente.
	 * @param time Ciclo do simulador
	 */
	public abstract void handleMessage(int time);
	
}
