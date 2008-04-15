/*
 * Copyright 2008 Andreas Holstenson
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.l4.crayon.persist.jpa.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Helper class that stores the current {@link EntityManager}.
 * 
 * @author Andreas Holstenson
 *
 */
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
