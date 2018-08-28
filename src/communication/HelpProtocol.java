package communication;

import java.util.ArrayList;

import rescuecore2.worldmodel.EntityID;

/**
 * @author jsilva
 * <p>Classe utilizada pelo Centro de comando de cada agente, para quando precisar 
 * solicitar um pedido de ajuda em um evento que só um agente não é suficiente
 * para resolvê-lo.
 */
public class HelpProtocol extends AbstractMessageProtocol {

	private EntityID agentDestiny;
	private EntityID placeToHelp;
	
	public HelpProtocol(int channel, char agentChar, 
			int time, EntityID myID, EntityID agentDestiny, EntityID placeToHelp) {
		super(channel, "C2A", agentChar, time, myID, 4, (agentDestiny.toString() + " " + placeToHelp.toString()));
		this.agentDestiny = agentDestiny;
		this.placeToHelp = placeToHelp;
	}
	
	public HelpProtocol(int channelReceived, String[] msgReceived) {
		super(channelReceived, msgReceived);
		this.agentDestiny = new EntityID(Integer.parseInt(msgReceived[5]));
		this.placeToHelp = new EntityID(Integer.parseInt(msgReceived[6]));
	}

	public EntityID getAgentDestiny() {
		return agentDestiny;
	}

	public EntityID getPlaceToHelp() {
		return placeToHelp;
	}
	
	/**
	 * De acordo com a lista de mensagens, verifica se nesta está contida aguma mensagem 
	 * do tipo pedido de ajuda.
	 * @param messages - Lista de mensagens utilizada pelo agente.
	 * @return - retorna um booleano dizendo se a lista contém ou não uma mensagem do tipo Pedido de ajuda.
	 */
	public static boolean hasHelpMsgToSend(ArrayList<AbstractMessageProtocol> messages) {
		boolean hasHelpMsg = false;

		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof HelpProtocol)
				hasHelpMsg = true;

		return hasHelpMsg;
	}
	
	/**
	 * Esse método é chamado quando quer se obter uma mensagem do tipo PEdido de ajuda 
	 * da lista passada como parâmetro.
	 * @param messages - LIsta de mensagens utilizada pelo agente
	 * @return - retorna um objeto do tipo {@link HelpProtocol} 
	 */
	public static HelpProtocol getHelpMsgFromList(ArrayList<AbstractMessageProtocol> messages) {
		HelpProtocol result = null;
		for (AbstractMessageProtocol mp : messages)
			if (mp instanceof HelpProtocol)
				result = (HelpProtocol) mp;
		return result;
	}

}
