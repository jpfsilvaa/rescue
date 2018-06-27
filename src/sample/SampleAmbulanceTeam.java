package sample;

import java.util.List;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;

/**
   A sample ambulance team agent.
 */
public class SampleAmbulanceTeam extends AbstractSampleAgent<AmbulanceTeam> {
    private Collection<EntityID> unexploredBuildings;
    private State state = State.PATRULHAR;
    int x = 0;
    int i = 0;
    private String msgFinal;
    private String[] resultado;
    private String entityChamado;
    private String posicaoChamado;
    private EntityID entityIncendio;
    private EntityID entityBloqueio;
    private EntityID msgID;
    private Human civilAtual, agenteRSoterrado;
    private boolean incendio = false;
    private boolean bloqueio = false;
	//private Eventos eventosAmb = new Eventos('c');
	private int id;
	private EntityID eventoFila = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	private enum State{
		PATRULHAR,
		RESGATAR_CIVIL,
		ATENDER_CHAMADO,
		EVENTO_EXTERIOR,
		UNLOAD, 
		CHAMADO_FILA,
		CIVIL_NEIGHBOURS,
		RESGATAR_AGENTE
	}

    @Override
    public String toString() {
        return "Sample ambulance team";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
        unexploredBuildings = new HashSet<EntityID>(buildingIDs);
        System.out.println("AMBULANCIA CONECTADA!");
        
        int[] path0 = {975, 275, 297, 296, 256, 270};
        paths.put(new Integer(21), path0);
        int[] path1 = {976, 279, 268, 256, 273, 330, 968};
        paths.put(new Integer(22), path1);
        int[] path2 = {279, 976, 975, 257, 270, 268};
        paths.put(new Integer(23), path2);
        int[] path3 = {297, 975, 976, 279, 968, 969, 330};
        paths.put(new Integer(24), path3);
        int[] path4 = {256, 268, 279, 976, 257, 270};
        paths.put(new Integer(25), path4);
        int[] path5 = {975, 275, 297, 296, 256, 270};
        paths.put(new Integer(26), path5);
        int[] path6 = {976, 279, 268, 256, 273, 330, 968};
        paths.put(new Integer(27), path6);
        int[] path7 = {279, 976, 975, 257, 270, 268};
        paths.put(new Integer(28), path7);
        int[] path8 = {297, 975, 976, 279, 968, 969, 330};
        paths.put(new Integer(29), path8);
        int[] path9 = {256, 268, 279, 976, 257, 270};
        paths.put(new Integer(30), path9);
    }
    
    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
    	
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            sendSubscribe(time, 1);
        }

        for (Command next : heard)
        {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	msgID = msg.getAgentID();
        	byte[] msgRaw = msg.getContent();
        	msgFinal = new String (msgRaw);
        	//System.out.println("-----(A-" + this.id + ") MENSAGEM RECEBIDA: " + msgFinal);
        	Logger.info(time + " (A-" + me().getID() + ") MENSAGEM RECEBIDA: " + msgFinal);
        	resultado = msgFinal.split(" ");
        }
        updateUnexploredBuildings(changed);
        
        if(resultado != null)
        {
	        if(resultado.length == 4)
	        {
		        if(resultado[1].equals("1"))
		        {
		        	if(state == State.RESGATAR_CIVIL || state == State.UNLOAD || !(ambEvents.isEmpty()))
		        	{
		        		EntityID civil = new EntityID(Integer.parseInt(resultado[2]));
		        		ambEvents.adicionarEvento(model, civil);
		        		//System.out.println("(A-" + this.id + ")ADICIONEI UM EVENTO");
		        		resultado = null;
		        	}
		        	else
		        	{
		        		entityChamado = resultado[2];
		        		posicaoChamado = resultado[3];
		        		//System.out.println("(A-" + this.id + ")FUI ATENDER CHAMADO");
		        		state = State.ATENDER_CHAMADO;
		        	}
		        }
		        else if(resultado[2].equals("atendendo"))
	        	{
	        		if(civilAtual != null)
	        		{
		        		if(!resultado[1].equals(me().getID().toString()) && resultado[3].equals(civilAtual.getID().toString()))
		        		{
		        			System.out.println("...(A" + this.id + ")CIVIL JA ESTÁ SENDO ATENDIDO! REMOVANDO CRENÇA...  + " + me().getPosition());
		        			Crenca.removerCrenca(civilAtual.getID(), crencas);
		        			patrulhar(time);
		        			state = state.PATRULHAR;
		        			civilAtual = null;
		        			resultado = null;
		        			return;
		        		}
	        		}
	        	}
	        }
	        else if(resultado.length == 1)
	        {
	        	if(resultado[0].equals("Ouch") || resultado[0].equals("Help"))
	        	{
	        		System.out.println("(A-" + this.id + ")HÁ UM CIVIL PEDINDO SOCORRO POR PERTO! ---> " + resultado[0] + " + " + me().getPosition());
	        		if(state == State.PATRULHAR)
	        		{
	        			state = State.CIVIL_NEIGHBOURS;
	        		}
	        		resultado = null;
	        	}
	        }
        }
        
        List<EntityID> caminho;
        
    	if(!someoneOnBoard() && state != State.RESGATAR_CIVIL && state != State.UNLOAD)
    	{
	        for(Human next : getTargets())
			{
				caminho = search.breadthFirstSearch(me().getPosition(), next.getPosition());
				if(caminho != null)
				{
					sendMove(time, caminho);
					if (next.getPosition().getValue() == me().getPosition().getValue())
					{
						if((next instanceof Civilian))
						{
							System.out.println("A-" + this.id + ") É UM CIVIL! SALVANDO...");
							civilAtual = next;
							if(!Crenca.existeIdCrenca(civilAtual.getID(), crencas))
							{
								Crenca civil = new Crenca(civilAtual.getID(), civilAtual.getPosition(), 1);
								crencas.add(civil);
							}
							System.out.println("A-" + this.id + ") estado = resgatar civil + " + me().getPosition());
							state = State.RESGATAR_CIVIL;
							break;
						}
						else if(next instanceof FireBrigade || next instanceof PoliceForce || next instanceof AmbulanceTeam)
						{
							System.out.println("A-" + this.id + ") É UM AGENTE DE RESGATE! SALVANDO...");
							agenteRSoterrado = next;
							if(!Crenca.existeIdCrenca(agenteRSoterrado.getID(), crencas))
							{
								Crenca agenteResgate = new Crenca(agenteRSoterrado.getID(), agenteRSoterrado.getPosition(), 1);
								crencas.add(agenteResgate);
							}
							System.out.println("A-" + this.id + ") estado = resgatar agente + " + me().getPosition());
							state = State.RESGATAR_AGENTE;
							break;
						}
					}
				}
				else
				{
					System.out.println("A-" + this.id + "CAMINHO NULO PARA CIVIL");
					state = State.PATRULHAR;
				}
			}
    	}
        
    	//AÇÕES FEITAS PELO RAIO DE PERCEPÇÃO DE AGENTE, UTILIZADO NO CÓDIGO DO RCRSCS(MODIFICADO)
    	if(state != State.UNLOAD)
    	{
	    	StandardEntity entity;
			for(EntityID id : changed.getChangedEntities())
			{
				entity = this.model.getEntity(id);
				if(entity instanceof Blockade)
				{
					Blockade blockade = (Blockade) entity;
					if(blockade.isPositionDefined() && blockade.isRepairCostDefined())
					{
			        	//VERIFICO SE JA ESTÁ NA CRENÇA. SE JA ESTIVER, NEM ENTRO AQUI E NEM FAÇO NADA
			        	if(!Crenca.existeIdCrenca(blockade.getID(), crencas) && !Crenca.existePosicaoCrenca(me().getPosition(), crencas))
			        	{
							//System.out.println("(A-" + this.id + ") 3 " + blockade.getID() + " colocado como crença");
				        	bloqueio = true;
				        	entityBloqueio = blockade.getID();
				    		Crenca crencaEscombro = new Crenca(blockade.getID(), me().getPosition(), 3);
				    		crencas.add(crencaEscombro);
				        	state = State.EVENTO_EXTERIOR;
			        	}
					}
				}
				else if(entity instanceof Building){
					Building building = (Building) entity;
					if(building.isFierynessDefined() && building.isBrokennessDefined())
					{
						if(building.getFieryness() > 1)
						{
				    		//VERIFICO SE JA ESTÁ NA CRENÇA. SE JA ESTIVER, NEM ENTRO AQUI E NEM FAÇO NADA
				    		if(!Crenca.existeIdCrenca(building.getID(), crencas))
				    		{
								//System.out.println("(A-" + this.id + ") 2 " + building.getID() + " colocado como crença");
					    		incendio = true;
					    		entityIncendio = building.getID();
					    		Crenca crencaIncendio = new Crenca(building.getID(), me().getPosition(), 2);
					    		crencas.add(crencaIncendio);
					    		state = State.EVENTO_EXTERIOR;
				    		}
						}
					}
				}
			}
    	}
    	
        if(location() instanceof Building)
        {
        	Building aux =  (Building) this.model.getEntity(me().getPosition());
        	try{
        		if(aux.isOnFire()){
	        		state = State.PATRULHAR;
	        		System.out.println("(A)==============SAINDOOOOOOOO");
	        	}
        	}catch(NullPointerException np)
        	{
        		//System.out.println("(A) NULO!!");
        	}
        }
    	
        switch(state)
        {
	        case PATRULHAR:
	        	if(ambEvents.isEmpty())
	        	{
	        		List<EntityID> prediosInexplorados = search.breadthFirstSearch(me().getPosition(), unexploredBuildings);
	        		if(prediosInexplorados != null)
	        		{
	        			sendMove(time, prediosInexplorados);
	        			//System.out.println("+++(A-" + this.id + ") ANDANDO POR PREDIO INEXPLORADOS");
	        		}
	        		else
	        		{
	        			try{
	        				patrulhar(time);
	        			}catch(NullPointerException np){
	        				System.out.println("(A-" + this.id + "random walk");
	        				sendMove(time, randomWalk());
	        			}
	        			//System.out.println("+++(A-" + this.id + ") PATRULHANDO");
	        		}
	        		break;
	        	}
	        	else
	        	{
	        		System.out.println("(A-" + this.id + ") Desenfileirando evento " + ambEvents.proximoEvento());
	        		eventoFila = ambEvents.executarEvento();
	        		break;
	        	}
	        case RESGATAR_CIVIL:
	        	if(civilAtual.getPosition().getValue() == me().getPosition().getValue())
	        	{
		        	if(civilAtual.getBuriedness() == 0)
		        	{
		        		sendLoad(time, civilAtual.getID());
		        		state = State.UNLOAD;
		        		break;
		        	}
		        	else
		        	{
		        		sendSpeak(time, 1, ("(A) " + me().getID() + " atendendo " + civilAtual.getID()).getBytes());
		        		sendRescue(time, civilAtual.getID());
		        		System.out.println("(A-" + this.id + ")Soterramento: " + civilAtual.getBuriedness() + " + " + me().getPosition());
		        		break;
		        	}
	        	}
	        	else
	        	{
	        		caminho = search.breadthFirstSearch(me().getID(), civilAtual.getPosition());
	        		if(caminho != null)
	        			sendMove(time, caminho);
	        		else
	        			state = State.PATRULHAR;
	        		break;
	        	}
	        case RESGATAR_AGENTE:
	        	if(agenteRSoterrado.getPosition().getValue() == me().getPosition().getValue())
	        	{
		        	if(agenteRSoterrado.getBuriedness() == 0)
		        	{
		        		sendLoad(time, agenteRSoterrado.getID());
		        		state = State.UNLOAD;
		        		break;
		        	}
		        	else
		        	{
		        		System.out.println(agenteRSoterrado.getID() + "  ->  " + agenteRSoterrado.getPosition() + " / " + agenteRSoterrado.getStamina());
		        		System.out.println(me().getID() + "  ->  " + me().getPosition() + " / " + me().getStamina());
		        		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		        		sendSpeak(time, 1, ("(A) " + me().getID() + " atendendo " + agenteRSoterrado.getID()).getBytes());
		        		sendRescue(time, agenteRSoterrado.getID());
		        		System.out.println("(A-" + this.id + ")Soterramento: " + agenteRSoterrado.getBuriedness() + " + " + me().getPosition());
		        		break;
		        	}
	        	}
	        	else
	        	{
	        		caminho = search.breadthFirstSearch(me().getID(), agenteRSoterrado.getPosition());
	        		if(caminho != null)
	        			sendMove(time, caminho);
	        		else
	        			state = State.PATRULHAR;
	        		break;
	        	}
	        case UNLOAD:
	        	Collection<StandardEntity> refuges = model.getEntitiesOfType(StandardEntityURN.REFUGE);
        		for(StandardEntity next: refuges)
        		{
	        		caminho = search.breadthFirstSearch(me().getPosition(), next.getID());
	        		if(caminho != null)
	        		{
		        		sendMove(time, caminho);
		        		if(location() instanceof Refuge)
		        		{
		        			sendUnload(time);
		        			Crenca.removerCrenca(civilAtual.getID(), crencas);
		        			state = State.PATRULHAR;
		        			break;
		        		}
	        		}
        		}
	        case ATENDER_CHAMADO:
	        	if(entityChamado != null)
	        	{
	        		EntityID entityCh = new EntityID(Integer.parseInt(entityChamado));
	        		//Civilian c = (Civilian) model.getEntity(entityCh);
	        		/*if(entityCh != null)
	        		{*/
	        			//List<EntityID> path = search.breadthFirstSearch(me().getPosition(), c.getPosition());
	        			List<EntityID> path = search.breadthFirstSearch(me().getPosition(), entityCh);
		        		if(path != null)
		        		{
		        			sendMove(time, path);
		        			System.out.println("(######A-" + this.id + ") CAMINHANDO PARA O CIVIL DO CHAMADO");
		        			//SE O AGENTE NÃO PERCEBER O CIVIL AUTOMATICAMENTE, NAO ESTÁ MAIS LÁ, OU MORREU
		        			//ENTÃO ENTRARÁ NESSE IF
		        			if(me().getPosition().getValue() == entityCh.getValue())
		        			{
		        				System.out.println("1");
	        		        	if(Crenca.existeIdCrenca(eventoFila, crencas))
	        		        	{
	        		        		System.out.println("2");
	        			        	civilAtual = (Human) model.getEntity(Crenca.procuraCrencaPos(1, eventoFila, crencas));
	        			        	if(civilAtual != null)
	        			        	{
	        			        		System.out.println("3");
	        			        		state = State.RESGATAR_CIVIL;
	        			        		break;
	        			        	}
	        			        	else
	        			        	{
	        			        		System.out.println("-3");
	        			        		state = State.PATRULHAR;
	        			        		break;
	        			        	}
	        		        	}
	        		        	else
	        		        	{
	        		        		System.out.println("-2");
	        		        		state = State.PATRULHAR;
	        		        		break;
	        		        	}
		        			}
		        			else
		        			{
		        				sendMove(time, path);
			        			System.out.println("2(######A-" + this.id + ") CAMINHANDO PARA O CIVIL DO CHAMADO + " + me().getPosition());
		        			}
		        			break;
		        		}
		        		else
		        		{
		        			System.out.println("-1");
		        			System.out.println("(A-" + this.id + ") Caminho nulo para " + entityCh);
		        			entityChamado = null;
		        			resultado = null;
		        			state = State.PATRULHAR;
		        			break;
		        		}
	        	}
	    		else
	    		{
	    			resultado = null;
	    			System.out.println("(A-" + this.id + ")CIVIL CHAMADO NULO");
	    			state = State.PATRULHAR;
	    		}
	        	break;
	        	
	        case EVENTO_EXTERIOR:
	        	if(incendio)
	        	{
	        		//VOLTAR A PRINTAR DEPOISSystem.out.println("(A-" + this.id + ") PROTOCOLO 2 " + entityIncendio + " " + me().getPosition());
	        		sendSpeak(time, 1, ("(A-" + me().getID() + ") 2 " + entityIncendio+ " " + me().getPosition()).getBytes());
	        		incendio = false;
	        		state = State.PATRULHAR;
	        		break;
	        	}
	        	if(bloqueio)
	        	{
	        		//VOLTAR A PRINTAR DEPOISSystem.out.println("(A-" + this.id + ") PROTOCOLO 3 " + entityBloqueio+ " " + me().getPosition());
	        		sendSpeak(time, 1, ("(A-" + me().getID() + ") 3 " + entityBloqueio+ " " + me().getPosition()).getBytes());
	        		bloqueio = false;
	        		state = State.PATRULHAR;
	        		break;
	        	}
	        case CHAMADO_FILA:
	        	if(Crenca.existeIdCrenca(eventoFila, crencas))
	        	{
		        	civilAtual = (Human) model.getEntity(Crenca.procuraCrencaPos(1, eventoFila, crencas));
	        		if(civilAtual != null)
	        		{
		        		caminho = search.breadthFirstSearch(me().getPosition(), civilAtual.getPosition());
		        		if(caminho != null)
		        		{
			        		sendMove(time, caminho);
			        		if(me().getPosition() == civilAtual.getID())
			        			state = State.RESGATAR_CIVIL;
			        		break;
		        		}
	        		}
	        		else
	        		{
	        			System.out.println("CIVIL DO CHAMADO ESTÁ NULO");
	        			state = State.PATRULHAR;
	        			break;
	        		}
	        	}
        		else
        		{
        			System.out.println("(A-" + this.id + ") EVENTO DA FILA JA FOI ATENDIDO");
        			state = State.PATRULHAR;
        			break;
        		}
	        case CIVIL_NEIGHBOURS:
	        	/*List<EntityID> vizinhos = (List<EntityID>) getNearestBuildings(); // Obter vizinhos do eu local atual por meio de algum método;
				EntityID obj = null;
				for(EntityID v : vizinhos)
				{
					if(model.getEntity(v) instanceof Building)
					{
						obj = v;
						break;
					}
				}*/
				state = State.PATRULHAR;		        				
			/*	List<EntityID> p = search.breadthFirstSearch(me().getPosition(), getNearestBuildings());
				System.out.println("++++++++++(A-" + this.id + ") ENCONTRANDO CIVIL PELO GRITO DE AJUDA!!!! + " + me().getPosition());
				sendMove(time, p);
			*/	
				//DEIXAR O AGENTE PERCEBER O CIVIL, E SALVÁ-LO
				break;
        }
    }
    
	@Override
	public void patrulhar(int time) {
    	int[] vector = paths.get(new Integer(id));
    	List<EntityID> path;
		path = search.breadthFirstSearch(me().getPosition(),new EntityID(vector[i]));
        if(me().getPosition().getValue() == vector[i])
        {
        	i++;
        	if(i > vector.length - 1)
        		i = 0;
        	return;
        }
        sendMove(time, path);
	}
    
	@Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
    }

    private boolean someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                Logger.debug(next + " is on board");
                return true;
            }
        }
        return false;
    }

    private List<Human> getTargets() {
        List<Human> targets = new ArrayList<Human>();
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)next;
            if (h == me()) {
                continue;
            }
            if (h.isHPDefined()
                && h.isBuriednessDefined()
                && h.isDamageDefined()
                && h.isPositionDefined()
                && h.getHP() > 0
                && (h.getBuriedness() > 0 || h.getDamage() > 0)) {
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), model));
        return targets;
    }

    private void updateUnexploredBuildings(ChangeSet changed) {
        for (EntityID next : changed.getChangedEntities()) {
            unexploredBuildings.remove(next);
        }
    }
    
    private Collection<EntityID> getNearestBuildings()
    {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        DistanceSorter d = new DistanceSorter(location(), model);
        int dist;
        int nearest = 999999999;
        for (StandardEntity next : e) 
        {
            if (next instanceof Building) 
            {
                Building b = (Building)next;
                dist = d.compare(b, me().getPosition(model));
                //if(dist < nearest) <--PARA TESTAR SOMENTE O PREDIO MAIS PROXIMO DE TODOS
                if(dist < 12000)
                {
                	result.add(b);
                	System.out.println("TESTE-> building: " + b.getID() + " distance: " + dist);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model)); //??
        return objectsToIDs(result);
    }
    
    private Collection<EntityID> getBurningBuildings() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building)next;
                //b.get
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model)); //??
        return objectsToIDs(result);
    }
    
    private List<EntityID> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<EntityID> result = new ArrayList<EntityID>();
        for (StandardEntity next : e) {
            Road r = (Road)next;
            if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
                result.add(r.getID());
            }
        }
        return result;
    }
    
    public EntityID getNearestBlockade() {
        return getNearestBlockade((Area)location(), me().getX(), me().getY());
    }
    

    /**
       Get the blockade that is nearest a point.
       @param area The area to check.
       @param x The X coordinate to look up.
       @param y The X coordinate to look up.
       @return The EntityID of the nearest blockade, or null if there are no blockades in this area.
    */
    
    public EntityID getNearestBlockade(Area area, int x, int y) { 	
    	double bestDistance = 0;
        EntityID best = null;
        Logger.debug("Finding nearest blockade");
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                Logger.debug("Checking " + blockadeID);
                StandardEntity entity = model.getEntity(blockadeID);
                Logger.debug("Found " + entity);
                if (entity == null) {
                	continue;
                }
                Pair<Integer, Integer> location = entity.getLocation(model);
                Logger.debug("Location: " + location);
                if (location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if(distance >= 10000)
                	continue;
                //System.out.println("(A)distance " + distance );
                if (best == null || distance < bestDistance) 
                {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        Logger.debug("Nearest blockade: " + best);
        return best;
    }
    
    public EntityID getNearestFireBuilding() {
        return getNearestFireBuilding((Area)location(), me().getX(), me().getY());
    }
    
    public EntityID getNearestFireBuilding(Area a, int x, int y)
    {
        EntityID best = null;
        double bestDistance = 0;
        Collection<EntityID> f = getBurningBuildings();
        //List<EntityID> neightbours = a.getNeighbours(); ---> NAO DÁ PRA USAR PQ O NEIGHBOURS RETORNA ROADS
        for (EntityID next : f)
        {
        	Building b = (Building) model.getEntity(next);
        	if(b == null)
        		continue;
        	Pair<Integer, Integer> location = b.getLocation(model);
        	if(location == null)
        		continue;
        	double dx = location.first() - x;
        	double dy = location.second() - y;
        	double distance = Math.hypot(dx, dy);
        	if(distance >= 15000)
        		continue;
            if (best == null || distance < bestDistance) 
            {
                bestDistance = distance;
                best = b.getID();
            }
        }
        return best;
    }
}