package newAgents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

public class MessageProtocol {
	private int code;
	private int channel;
	private EntityID myID;
	private String type;
	private String details;
	private char agent;
	private int time;
	
	// Construtor para mensagem de um agente para central
	public MessageProtocol(int channel, String type, char agent, int time, EntityID myID, int code, String details) {
		this.channel = channel;
		this.type = type;
		this.agent = agent;
		this.time = time;
		this.myID = myID;
		this.code = code;
		this.details = details;
	}
	
	// C2C A||F||P 'time' entityID_central_destinataria entityID_evento entityID_local_do_Agente_que_avistou_evento
	// Costrutor para mensagem de uma central para -> uma central para -> outra central ou agente
	public MessageProtocol(int channel, String type, char agent, int time, EntityID myID, String details) {
		this.channel = channel;
		this.type = type;
		this.agent = agent;
		this.time = time;
		this.myID = myID;
		this.details = details;
		this.code = 3;
	}
	
	
	/**
	 * Esse método define a mensagem que tem prioridade 
	 * na fila de mensagens que foi que é passada por parâmetro de 
	 * acordo com o código dela (0, 1, 2) e esses códigos são tratados 
	 * como se realmente se fossem pesos, ou seja, se houver uma mensagem 
	 * de código 2 e uma de código 1 na fila, a de código 2 será definida como 
	 * prioritária.
	 * @param messages - Fila de mensagens que contém todas as mensagens que 
	 * o agente deseja enviar até o momento da chamada desta função.
	 * @return - Retorna um objeto do tipo MessageProtocol, que foi definida como
	 * prioritária no momento.
	 */
	public static ArrayList<MessageProtocol> setFirstMessagesOnQueue(ArrayList<MessageProtocol> messages) {
		ArrayList<MessageProtocol> messagesSortedByCode = messages;
		
		Collections.sort(messagesSortedByCode, Comparator.comparingInt(MessageProtocol::getCode).reversed());
		
//		if (messagesSorted != null)
//			MessageProtocol m = messages.remove(0);
			
		return messagesSortedByCode;
	}
	
	public String getEntireMessage() {
		return this.getType() + " " + this.getAgent() + " " + this.getTime() + 
				" " + this.getMyID() + " " + (this.getCode() == 3 ? "" : this.getCode() + " ") + this.getDetails();
	}
	
	public int getCode() {
		return this.code;
	}
	
	public int getChannel() {
		return this.channel;
	}
	
	public EntityID getMyID() {
		return this.myID;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getDetails() {
		return this.details;
	}
	
	public char getAgent() {
		return this.agent;
	}
	
	public int getTime() {
		return this.time;
	}
}
