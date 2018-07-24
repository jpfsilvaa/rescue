package sample;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

public class Eventos /*extends AbstractQueue<EntityID>*/{
	
	private Queue<EntityID> q;
	
	private int[] buriednesses;
	private Civilian c;
	private Civilian[] cAux;
	private int aux;
	private int[] fierynesses;
	private Building b;
	private Building[] bAux;
	private int[] repairCosts;
	private Blockade bl;
	private Blockade[] blAuxs;
	private char tipo;
	private int k;
	
	public Eventos(char tipo)
	{
		this.tipo = tipo;
		this.q = new LinkedList<EntityID>();
	}
	
	public int tamanhoFila()
	{
		return q.size();
	}
	
	public void adicionarEvento(WorldModel<? extends StandardEntity> model, EntityID x)
	{
		q.offer(x);
		//this.offer(x);
/*		switch(this.tipo)
		{
			case 'c':
					ordenarEventosCivil(model, q); break;
			case 'f':
					ordenarEventosFire(model, q); break;
			case 'b':
					ordenarEventosBlock(model, q); break;
		}*/
	}
	
	public synchronized EntityID executarEvento()
	{
		return q.poll();
	}
	
	public synchronized boolean isEmpty()
	{
		if(q.isEmpty())
			return true;
		else
			return false;
	}
	
	public synchronized EntityID proximoEvento()
	{
		return q.peek();
	}
	
	private void ordenarEventosCivil(WorldModel<? extends StandardEntity> model, Queue<EntityID> queue)
	{
		//Ordena pelo quanto civil esta mais soterrado
		cAux = null;
		buriednesses = null;
		
		for(int i = 0; i < queue.size(); i++)
		{
			c = (Civilian) model.getEntity(queue.poll());
			if(c == null)
				return;
			
			System.out.println(c.getBuriedness());
			buriednesses[i] = c.getBuriedness();
			cAux[i] = c; 
		}
		
		k = buriednesses.length - 1;
		
		for(int i = 0; i <= buriednesses.length; i++)
		{
			for(int j = 0; j < k; j++)
			{
				if(buriednesses[j] < buriednesses[j+1])
				{
					aux = buriednesses[j];
					buriednesses[j] = buriednesses[j+1];
					buriednesses[j+1] = aux;
				}
			}
		}
		
		for(int i = 0; i < buriednesses.length; i++)
		{
			for(int j = 0; j < cAux.length; j++)
			{
				if(buriednesses[i] == cAux[j].getBuriedness())
					queue.offer(cAux[j].getID());
			}
		}
	}
		
	private void ordenarEventosFire(WorldModel<? extends StandardEntity> model, Queue<EntityID> queue)
	{
		//Ordena por intensidade de incendio mais alta
		bAux = null;
		fierynesses = null;
		for(int i = 0; i < queue.size(); i++)
		{
			System.out.println(queue.peek());
			b = (Building) model.getEntity(queue.poll());
			if(b == null)
				return;
			System.out.println(b.getFieryness());
			fierynesses[i] = b.getFieryness();
			bAux[i] = b;
		}
		
		k = fierynesses.length - 1;
		
		for(int i = 0; i < fierynesses.length; i++)
		{
			for(int j = 0; j < k; j++)
			{
				if(fierynesses[j] < fierynesses[j+1])
				{
					aux = fierynesses[j];
					fierynesses[j] = fierynesses[j+1];
					fierynesses[j+1] = aux;
				}
			}
		}
		
		for(int i = 0; i < fierynesses.length; i++)
		{
			for(int j = 0; j < bAux.length; j++)
			{
				if(fierynesses[i] == bAux[j].getFieryness())
					queue.offer(bAux[j].getID());
			}
		}
	}

	private void ordenarEventosBlock(WorldModel<? extends StandardEntity> model, Queue<EntityID> queue)
	{
		//Ordena bloqueios pelo custo de reparo deles
		blAuxs = null;
		repairCosts = null;
		for(int i = 0; i < queue.size(); i++)
		{
			bl = (Blockade) model.getEntity(queue.poll());
			if(bl == null)
				return;
			repairCosts[i] = bl.getRepairCost();
			blAuxs[i] = bl;
		}
		
		k = repairCosts.length - 1;
		
		for(int i = 0; i < repairCosts.length; i++)
		{
			for(int j = 0; j < k; j++)
			{
				if(repairCosts[j] < repairCosts[j+1])
				{
					aux = repairCosts[j];
					repairCosts[j] = repairCosts[j+1];
					repairCosts[j+1] = aux;
				}
			}
		}
		
		for(int i = 0; i < repairCosts.length; i++)
		{
			for(int j = 0; j < blAuxs.length; j++)
			{
				if(repairCosts[i] == blAuxs[j].getRepairCost())
					queue.offer(blAuxs[j].getID());
			}
		}
		
	}

/*	@Override
	public boolean offer(EntityID e) {
		q.offer(e);
		return true;
	}

	@Override
	public EntityID poll() {
		if(!q.isEmpty())
			return q.poll();
		else
			return null;
	}

	@Override
	public EntityID peek() {
		if(!q.isEmpty())
			return q.peek();
		else
			return null;
	}

	@Override
	public Iterator<EntityID> iterator() {
		// 
		return null;
	}

	@Override
	public int size() {
		return q.size();
	}*/	
}
