package communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import newAgents.AbstractAgent;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.SampleSearch;

/**
 * @author jsilva
 *<p>Classe abstrata que é herdada por todas as classes que 
 *desejam fazer um tratamento de mensagens enviadas e 
 *recebidas pelos agentes e por suas centrais.
 */
public abstract class AbstractMessageProtocol {
	private int code;
	private int channel;
	private EntityID senderID;
	private String type;
	private String details;
	private char agentChar;
	private int time;

	// Construtor para mensagem de um agente para central
	public AbstractMessageProtocol(int channel, String type, char agentChar, int time, EntityID senderID, int code,
			String details) {
		this.channel = channel;
		this.type = type;
		this.agentChar = agentChar;
		this.time = time;
		this.senderID = senderID;
		this.code = code;
		this.details = details;
	}
	
	// Construtor para recebimento de mensagens
	public AbstractMessageProtocol(int channel, String[] msgReceived) {
		this.channel = channel;
		this.type = msgReceived[0];
		this.agentChar = msgReceived[1].charAt(0);
		this.time = Integer.parseInt(msgReceived[2]);
		this.senderID = new EntityID(Integer.parseInt(msgReceived[3]));
		this.code = Integer.parseInt(msgReceived[4]);
		this.details = Arrays.toString(AbstractAgent.subArray(msgReceived, 5, msgReceived.length));
	}
	
	// Construtor para mensagem de uma central para outra central
	public AbstractMessageProtocol(int channel, int code, String[] msgReceived) {
		this.channel = channel;
		this.type = msgReceived[0];
		this.agentChar = msgReceived[1].charAt(0);
		this.time = Integer.parseInt(msgReceived[2]);
		this.senderID = new EntityID(Integer.parseInt(msgReceived[3]));
		this.code = 3;
		this.details = Arrays.toString(AbstractAgent.subArray(msgReceived, 4, msgReceived.length));
	}

	/**
	 * <p>Esse método define a mensagem que tem prioridade na fila de mensagens que foi
	 * que é passada por parâmetro de acordo com o código dela (0, 1, 2) e esses
	 * códigos são tratados como se realmente se fossem pesos, ou seja, se houver
	 * uma mensagem de código 2 e uma de código 1 na fila, a de código 2 será
	 * definida como prioritária.
	 * 
	 * @param messages
	 *            - Fila de mensagens que contém todas as mensagens que o agente
	 *            deseja enviar até o momento da chamada desta função.
	 * @return - Retorna um objeto do tipo MessageProtocol, que foi definida como
	 *         prioritária no momento.
	 */
	public static ArrayList<AbstractMessageProtocol> setFirstMessagesOnQueue(ArrayList<AbstractMessageProtocol> messages) {
		ArrayList<AbstractMessageProtocol> messagesSortedByCode = messages;

		Collections.sort(messagesSortedByCode, Comparator.comparingInt(AbstractMessageProtocol::getCode).reversed());

		return messagesSortedByCode;
	}

	public String getEntireMessage() {
		return this.getType() + " " + this.getAgent() + " " + this.getTime() + " " + this.getSenderID() + " "
				+ this.getCode() + " " + this.getDetails();
	}

	public int getCode() {
		return this.code;
	}

	public int getChannel() {
		return this.channel;
	}

	public EntityID getSenderID() {
		return this.senderID;
	}

	public String getType() {
		return this.type;
	}

	public String getDetails() {
		return this.details;
	}

	public char getAgent() {
		return this.agentChar;
	}

	public int getTime() {
		return this.time;
	}
}
