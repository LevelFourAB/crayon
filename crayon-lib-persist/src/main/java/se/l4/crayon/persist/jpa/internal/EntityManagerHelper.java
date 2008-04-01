package se.l4.crayon.persist.jpa.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerHelper
{
	private static ThreadLocal<EntityManager> current;
	
	private static ConcurrentMap<String, EntityManagerFactory> factories;
	
	static
	{
		current = new ThreadLocal<EntityManager>();
		
		factories = new ConcurrentHashMap<String, EntityManagerFactory>();
	}
	
	private EntityManagerHelper()
	{
	}
	
	public static EntityManager getCurrent()
	{
		return current.get();
	}
	
	public static void setCurrent(EntityManager manager)
	{
		current.set(manager);
	}
	
	public static void clearCurrent()
	{
		current.remove();
	}
	
	public static EntityManagerFactory getFactory(String id)
	{
		EntityManagerFactory factory = factories.get(id);
		if(factory == null)
		{
			factory = Persistence.createEntityManagerFactory(id);
			factories.put(id, factory);
		}
		
		return factory;
	}
}
