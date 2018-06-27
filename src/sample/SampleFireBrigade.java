package sample;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.Timestep;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;

/**
   A sample fire brigade agent. //UM AGENTE DE BRIGADA DE INCENDIO PADRAO
 */
public class SampleFireBrigade extends AbstractSampleAgent<FireBrigade> {
    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";
    private int id;
    private int maxWater;																
    private int maxDistance;
    private int maxPower;
    private State state = State.PATRULHAR;
    int x = 0;
    int i = 0;
    private String msgFinal;
    private String entityChamado;
    private String posicaoChamado;
    private EntityID entityCivil;
    private EntityID entityBloqueio;
    private String[] resultado;
	Timestep t;
	//private Eventos fireEvents = new Eventos('f');
	private EntityID incendioAtual;
    private boolean escombro = false;
    private boolean civil = false;
    
    //private HashMap <Integer, int[]> paths = new HashMap <Integer, int[]>();
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	private enum State{
    	PATRULHAR,
    	APAGAR_INCENDIO,
    	ATENDER_CHAMADO,
    	EVENTO_EXTERNO,
    	RECARREGAR_TANQUE,
    	CHAMADO_FILA
    }

    
    @Override
    public String toString() {
        return "Sample fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        System.out.println("BRIGADA DE INCENDIO CONECTADA!");
        Logger.info("Sample fire brigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);
    
        int[] path0 = {297, 975, 976, 279, 968, 969, 330};
        paths.put(new Integer(1), path0);
        int[] path1 = {256, 268, 279, 976, 257, 270};
        paths.put(new Integer(2), path1);
        int[] path2 = {976, 279, 268, 256, 273, 330, 968};
        paths.put(new Integer(3), path2);
        int[] path3 = {279, 976, 975, 257, 270, 268};
        paths.put(new Integer(4), path3);
        int[] path4 = {268, 256, 296, 297, 257, 273, 969, 279};
        paths.put(new Integer(5), path4);
        int[] path5 = {297, 975, 976, 279, 968, 969, 330};
        paths.put(new Integer(6), path5);
        int[] path6 = {256, 268, 279, 976, 257, 270};
        paths.put(new Integer(7), path6);
        int[] path7 = {976, 279, 268, 256, 273, 330, 968};
        paths.put(new Integer(8), path7);
        int[] path8 = {279, 976, 975, 257, 270, 268};
        paths.put(new Integer(9), path8);
        int[] path9 = {268, 256, 296, 297, 257, 273, 969, 279};
        paths.put(new Integer(8), path9);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
    	
    	if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
    	
        for (Command next : heard)
        {
        	Logger.debug("Heard" + next);
        	AKSpeak msg = (AKSpeak) next;
        	byte[] msgRaw = msg.getContent();
        	msgFinal = new String (msgRaw);
        	//System.out.println("--------------(A) MENSAGEM RECEBIDA: " + msgFinal);
        	Logger.info("(F-" + me().getID() + ") MENSAGEM RECEBIDA: " + msgFinal);
        	resultado = msgFinal.split(" ");
        }
        
       //System.out.println("(F)ESTADO: " + state);
        
        List<EntityID> caminho;
        FireBrigade me = me();
        
        if(resultado != null && resultado.length == 4)
        {
	        if(resultado[1].equals("2"))
	        {
	        	if(state == State.APAGAR_INCENDIO || !(fireEvents.isEmpty()) || state == State.RECARREGAR_TANQUE)
	        	{
	        		EntityID incendio = new EntityID(Integer.parseInt(resultado[3]));
	        		fireEvents.adicionarEvento(model, incendio);
	        		//System.out.println("(F-" + this.id + ")ADICIONEI UM EVENTO");
	        		resultado = null;
	        	}
	        	else
	        	{
	        		entityChamado = resultado[2];
	        		posicaoChamado = resultado[3];
	        		state = State.ATENDER_CHAMADO;
	        	}
	        }
	        /*else if(resultado.length == 1)
	        {
	        	if(resultado[0].equals("Ouch") || resultado[0].equals("Help"))
	        	{
	        		System.out.println("(F-" + this.id + ")HÁ UM CIVIL PEDINDO SOCORRO POR PERTO!");
	        		entityCivil = new EntityID(0);
	        		civil = true;
	        		state = State.EVENTO_EXTERNO;
	        		resultado = null;
	        	}
	        }*/
        }
        
		if(location() instanceof Refuge && me().getWater() < maxWater)
		{
			sendRest(time);
			System.out.println("(F-" + this.id + ") Water: " + me.getWater() + "/" + maxWater);				
		}
        
        //PERCEBER INCENDIO
        Collection<EntityID> BurningB = getBurningBuildings();
		Building b = null;
		if(state != State.RECARREGAR_TANQUE && state != State.CHAMADO_FILA)
		{
			for(EntityID next : BurningB)
			{
				b = (Building)model.getEntity(next);
				if(model.getDistance(getID(), next) <= maxDistance && b.getFieryness() >= 1)
				{
					if(!(location() instanceof Refuge))
					{
						incendioAtual = next;
						if(!Crenca.existeIdCrenca(incendioAtual, crencas))
						{
							Crenca incendio = new Crenca(incendioAtual, incendioAtual, 2);
							crencas.add(incendio);
						}
						state = State.APAGAR_INCENDIO;
					}
				}
			}
		}
        
        //PERCEBER QUE O TANQUE ESTA VAZIO
		if(me.isWaterDefined() && me.getWater() == 0)
			state = State.RECARREGAR_TANQUE;
		
    	//AÇÕES FEITAS PELO RAIO DE PERCEPÇÃO DE AGENTE, UTILIZADO NO CÓDIGO DO RCRSCS(MODIFICADO)
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
						//System.out.println("(F-" + this.id + ") 3 " + blockade.getID() + " colocado como crença");
			        	escombro = true;
			        	entityBloqueio = blockade.getID();
			        	Crenca crencaEscombro = new Crenca(blockade.getID(), me().getPosition(), 3);
			        	crencas.add(crencaEscombro);
			        	state = State.EVENTO_EXTERNO;
		        	}
				}
			}
			else if(entity instanceof Civilian)
			{
				Civilian victim = (Civilian) entity;
				if(victim.isPositionDefined() && victim.isHPDefined() && victim.isBuriednessDefined() && victim.isDamageDefined())
				{
		        	//VERIFICO SE JA ESTÁ NA CRENÇA. SE JA ESTIVER, NEM ENTRO AQUI E NEM FAÇO NADA
					if(!Crenca.existeIdCrenca(victim.getID(), crencas))
					{
						//System.out.println("(F-" + this.id + ") 1 " + victim.getID() + " colocado como crença");
						civil = true;
						entityCivil = victim.getPosition();
						Crenca crencaCivil = new Crenca(victim.getID(), victim.getPosition(), 1);
						crencas.add(crencaCivil);
						state = State.EVENTO_EXTERNO;
					}
				}
			}
		}
        
        if(location() instanceof Building)
        {
        	Building aux = new Building(new EntityID(me().getPosition().getValue()));
        	try{
        		if(aux.isOnFire()){
	        		state = State.PATRULHAR;
	        		System.out.println("(F)===============SAINDOOOOOOOO");
	        	}
        	}catch(NullPointerException np)
        	{
        		//System.out.println("(F) NULO!!");
        	}
        }
		
		//System.out.println("----------------------(F-" + this.id + ")ESTADO: " + state);
		
        switch(state)
        {
        	case PATRULHAR:
        		if(fireEvents.isEmpty())
        		{
        			patrulhar(time);
        			break;
        		}
        		else
        		{
        			//VOLTAR A PRINTAR DEPOISSystem.out.println("(F-" + this.id + ") Desenfileirando evento " + fireEvents.proximoEvento());
	        		incendioAtual = (fireEvents.executarEvento());
	        		if(incendioAtual != null)
	        			state = State.CHAMADO_FILA;
	        		else
	        			patrulhar(time);
	        		break;
        		}
        	case APAGAR_INCENDIO:
        		b = (Building) model.getEntity(incendioAtual);
        		if(b != null)
        		{
        			try{
		        		if(b.getFieryness() == 0 || b.getFieryness() >= 6)
		        		{
		        			Crenca.removerCrenca(incendioAtual, crencas);
		        			state = State.PATRULHAR;
		        			break;
		        		}
		        		else
		        		{
		        			if(model.getDistance(me.getPosition(), b.getID()) <= maxDistance && !(location() instanceof Refuge) 
		        					&& (location() instanceof Road))
		        			{
			        			Building auxB = (Building) model.getEntity(incendioAtual);
			        			if(auxB.getFieryness() >= 4)
			        			{
			        				state = State.PATRULHAR;
			        				break;
			        			}
			        			else
			        			{
				        			sendExtinguish(time, incendioAtual, maxPower);
				        			//System.out.println("(F-" + this.id + ")Intensidade do incendio atual: " + auxB.getFieryness());
				        			break;
			        			}
		        			}
		        			else
		        			{
		        				List<EntityID> vizinhos = b.getNeighbours();
		        				EntityID obj = null;
		        				for(EntityID v : vizinhos)
		        					if(model.getEntity(v) instanceof Road)
		        					{
		        						obj = v;
		        						break;
		        					}
		        						        				
		        				List<EntityID> p = search.breadthFirstSearch(me().getPosition(), obj);
		        				//System.out.println("++++++++++++++(F - " + this.id + ") Ainda estou fora da distância máxima!");
		        				sendMove(time, p);
		        				break;
		        			}
		        		}
        			}catch(NullPointerException np){
        				state = State.PATRULHAR;
        				break;
        			}
        		}
        		else
        			state = State.PATRULHAR;
        		break;
        	case ATENDER_CHAMADO:
        		EntityID aux = new EntityID(Integer.parseInt(posicaoChamado)); //BOMBEIRO ESTAVA USANDO O ENTITY DO EVENTO, AÍ ENTRAVA NO PREDIO PEGANDO FOGO
        		List<EntityID> path = search.breadthFirstSearch(me().getPosition(), aux);
        		if(path != null)
        		{
        			sendMove(time, path);
        			if(me.getPosition().getValue() == aux.getValue())
        				state = State.PATRULHAR;
        			break;
        		}
        		else
        		{
        			System.out.println("\\(F-" + this.id + ") CAMINHO NULO PARA CHAMADO");
        			state = State.PATRULHAR;
        		}
	        	break;
        	case EVENTO_EXTERNO:
	        	if(civil)
	        	{
	        		//VOLTAR A PRINTAR DEPOISSystem.out.println("(F-" + this.id + ") PROTOCOLO 1 " + entityCivil + " " + me().getPosition());
	        		sendSpeak(time, 1, ("(F-" + me().getID() + ") 1 " + entityCivil + " " + me().getPosition()).getBytes());
	        		civil = false;
	        		state = State.PATRULHAR;
	        		break;
	        	}
	        	if(escombro)
	        	{
	        		//VOLTAR A PRINTAR DEPOISSystem.out.println("(F-" + this.id + ") PROTOCOLO 3 " + entityBloqueio + " " + me().getPosition());
	        		sendSpeak(time, 1, ("(F-" + me().getID() + ") 3 " + entityBloqueio + " " + me().getPosition()).getBytes());
	        		escombro = false;
	        		state = State.PATRULHAR;
	        		break;
	        	}
        	case RECARREGAR_TANQUE:
    			caminho = search.breadthFirstSearch(me().getPosition(), refugeIDs);
    			if(caminho != null)
    			{
    				sendMove(time, caminho);
    				if(me().getWater() == maxWater)
    				{
    					if(Crenca.procuraCrencaTipo(2, crencas) != null)
    					{
    						incendioAtual = Crenca.procuraCrencaTipo(2, crencas);
    						state = State.APAGAR_INCENDIO;
    						//VOLTAR A PRINTAR DEPOISSystem.out.println("\\(F - " + this.id + ") VOLTANDO PARA O INCENDIO ATUAL " + incendioAtual);
    						break;
    					}
    					else
    					{
    						state = State.PATRULHAR;
    						break;
    					}
    				}
    			}
        		break;
        	case CHAMADO_FILA:
        		if(Crenca.existePosicaoCrenca(incendioAtual, crencas))
        		{
	        		caminho = search.breadthFirstSearch(me().getPosition(), incendioAtual);
	        		if(caminho != null)
	        		{
		        		if(me().getPosition().getValue() == incendioAtual.getValue())
		        		{
		        			//VOLTAR A PRINTAR DEPOISSystem.out.println("\\(F-" + this.id + ") CHEGUEI NO EVENTO DA FILA " + incendioAtual);
		        			incendioAtual = Crenca.procuraCrencaPos(2, incendioAtual, crencas);
		        			state = State.APAGAR_INCENDIO;
		        			break;
		        		}
		        		else
		        		{
		        			sendMove(time, caminho);
		        			break;
		        		}
	        		}
	        		else
	        		{
	        			//VOLTAR A PRINTAR DEPOISSystem.out.println("\\(F-" + this.id + ") CAMINHO NULO PARA EVENTO DA FILA " + incendioAtual);
	        			state = State.PATRULHAR;
	        			break;
	        		}
        		}
        		else
        		{
        			//VOLTAR A PRINTAR DEPOISSystem.out.println("\\(F-" + this.id + ") EVENTO DA FILA JA FOI ATENDIDO " + incendioAtual);
        			state = State.PATRULHAR;
        			break;
        		}
        }
    }
    
	@Override
	public void patrulhar(int time) {
    	int[] vector = paths.get(new Integer(id));
    	List<EntityID> path;
    	//System.out.println(vector[i]);
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
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
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

    private List<EntityID> planPathToFire(EntityID target) {
        // Try to get to anything within maxDistance of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));//??
    }
    
    private List<EntityID> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<EntityID> result = new ArrayList<EntityID>();
        for (StandardEntity next : e) {
            Road r = (Road)next;
            if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
            	//System.err.println("Blockade size = " + r.getBlockades().size());
            	/*for(EntityID b : r.getBlockades())
            	{
            		System.err.println(b.getValue());
            	}*/
                if(r.getBlockades().size() > 1)
                	result.add(r.getID());
            }
        }
        return result;
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
                if(distance >= 3000)
                	continue;
                //System.out.println("(F)distance " + distance );
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
    
}