package sample;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import rescuecore2.worldmodel.EntityID;

public class Crenca {
	private EntityID idCrenca;
	private EntityID posicaoCrenca;
	private int evento; //1-civil, 2-incendio 3-escombro

	public Crenca(EntityID idCrenca, EntityID posicaoCrenca, int evento)
	{
		this.idCrenca = idCrenca;
		this.posicaoCrenca = posicaoCrenca;
		this.evento = evento;
	}
	
	public synchronized static boolean existeIdCrenca(EntityID c, ArrayList<Crenca> cr)
	{
		try{
			for(Crenca next : cr)
			{
				if(next.idCrenca.getValue() == c.getValue())
					return true;
			}
			
		}catch(ConcurrentModificationException | NullPointerException ex) {}
			
		return false;
	}
	
	public synchronized static boolean existePosicaoCrenca(EntityID posicao, ArrayList<Crenca> cr)
	{
		try{
			for(Crenca next : cr)
			{
				if(next.posicaoCrenca.getValue() == posicao.getValue())
					return true;
			}
			
		}catch(ConcurrentModificationException ex) {}
			
		return false;
	}
	
	public static void removerCrenca(EntityID c, ArrayList<Crenca> cr)
	{
		try{
			for(int i = 0; i < cr.size(); i++)
			{
				//System.out.println("ListaCrença: " + cr.get(i).idCrenca);
				if(cr.get(i).idCrenca.getValue() == c.getValue())
				{
					cr.remove(i);
					//System.out.println("----------------CRENÇA " + c + " REMOVIDA!");
					return;
				}
			}
		}catch(ConcurrentModificationException ex) {
			System.out.println("ERRO AO REMOVER A CRENÇA!");
		}
	}
	
	public static EntityID procuraCrencaPos(int evento, EntityID posicao, ArrayList<Crenca> cr)
	{
		EntityID id = null;
		for(int i = 0; i < cr.size(); i++)
		{
			if(cr.get(i).posicaoCrenca.getValue() == posicao.getValue() && cr.get(i).evento == evento)
				id = cr.get(i).idCrenca;
		}
		return id;
		
	}
	
	public static EntityID procuraCrencaTipo(int tipo, ArrayList<Crenca> cr)
	{
		EntityID e = null;
		for(int i = cr.size()-1; i >= 0; i--) //Procura o ultimo evento (do tipo inserido) que foi adicionado
		{
			if(cr.get(i).evento == tipo)
			{
				e = cr.get(i).idCrenca;
				break;
			}
		}
		return e;
	}

	public EntityID getIdCrenca() {
		return idCrenca;
	}

	public void setIdCrenca(EntityID idCrenca) {
		this.idCrenca = idCrenca;
	}

	public int getEvento() {
		return evento;
	}

	public void setEvento(int evento) {
		this.evento = evento;
	}
	
	
	
}
