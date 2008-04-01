package se.l4.crayon.persist.jpa.internal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import se.l4.crayon.persist.jpa.JpaHelper;

public class PersistenceMethodInterceptor
	implements MethodInterceptor
{

	public Object invoke(MethodInvocation invocation)
		throws Throwable
	{
		// Check if we have entity manager, if so do not do anything special
		EntityManager manager = EntityManagerHelper.getCurrent();
		if(manager != null && manager.isOpen())
		{
			return invocation.proceed();
		}
		
		// Retrieve the correct entity manager
		PersistenceContext annotation = invocation.getMethod()
			.getAnnotation(PersistenceContext.class);
		
		JpaHelper.enterPersistence(annotation.name());
		
		try
		{
			return invocation.proceed();
		}
		finally
		{
			JpaHelper.exitPersistence();
		}
	}

}
