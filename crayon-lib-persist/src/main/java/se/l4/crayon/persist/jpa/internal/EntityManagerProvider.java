package se.l4.crayon.persist.jpa.internal;

import javax.persistence.EntityManager;

import com.google.inject.Provider;


public class EntityManagerProvider
	implements Provider<EntityManager>
{
	public EntityManager get()
	{
		return EntityManagerHelper.getCurrent();
	}

	@Override
	public String toString()
	{
		return "EntityManagerProvider";
	}
}
