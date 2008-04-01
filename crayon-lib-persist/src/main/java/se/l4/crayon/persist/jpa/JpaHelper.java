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
