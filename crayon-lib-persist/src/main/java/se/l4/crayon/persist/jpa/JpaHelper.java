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
package se.l4.crayon.persist.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import se.l4.crayon.persist.jpa.internal.EntityManagerHelper;

/**
 * Helper class for JPA that can be used to enter/exit a mode where it is
 * possible to access a {@link EntityManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class JpaHelper
{
	private JpaHelper()
	{
	}
	
	/**
	 * This method will enter a persistence mode, associating a
	 * {@link EntityManager} with the current thread.
	 * 
	 * @param id
	 */
	public static void enterPersistence(String id)
	{
		EntityManagerFactory factory =
			EntityManagerHelper.getFactory(id);
		
		EntityManager manager = factory.createEntityManager();
		EntityManagerHelper.setCurrent(manager);
	}
	
	/**
	 * This method will exit the persistence mode. Will remove the registered
	 * {@link EntityManager} from the thread.
	 */
	public static void exitPersistence()
	{
		EntityManager manager = EntityManagerHelper.getCurrent();
		
		if(manager != null)
		{
			manager.close();
			EntityManagerHelper.clearCurrent();
		}
	}
	
	/**
	 * Retrieve the entity manager associated with the thread. Only possible
	 * if within a {@link PersistenceMethod} or if 
	 * {@link #enterPersistence(String)} has been manually used.
	 *  
	 * @return
	 * 		entity manager if it exist for thread, otherwise {@code null}
	 */
	public static EntityManager getEntityManager()
	{
		return EntityManagerHelper.getCurrent();
	}
}
