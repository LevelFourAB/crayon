package se.l4.crayon.persist.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;

import se.l4.crayon.annotation.Description;
import se.l4.crayon.persist.Transactional;
import se.l4.crayon.persist.jpa.internal.EntityManagerProvider;
import se.l4.crayon.persist.jpa.internal.PersistenceMethodInterceptor;
import se.l4.crayon.persist.jpa.internal.TransactionalMethodInterceptor;

/**
 * Module for JPA support.
 * 
 * @author Andreas Holstenson
 *
 */
public class JpaModule
{
	@Description
	public void register(Binder binder)
	{
		// Bind EntityManager to a custom provider that depends on the
		// method interceptor that is later bound
		binder
			.bind(EntityManager.class)
			.toProvider(EntityManagerProvider.class);
		
		// All methods that are annotated with PersistanceContext should be 
		// intercepted so they have a EntityManager available
		binder.bindInterceptor(
			Matchers.any(), 
			Matchers.annotatedWith(PersistenceContext.class),
			new PersistenceMethodInterceptor()
		);
		
		// Bind handler for Transactional methods
		binder.bindInterceptor(
			Matchers.any(), 
			Matchers.annotatedWith(Transactional.class),
			new TransactionalMethodInterceptor()
		);
	}
}
