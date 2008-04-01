package se.l4.crayon.persist.jpa.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TransactionalMethodInterceptor
	implements MethodInterceptor
{

	public Object invoke(MethodInvocation invocation)
		throws Throwable
	{
		EntityManager manager = EntityManagerHelper.getCurrent();
		EntityTransaction transaction = manager.getTransaction();
		
		if(transaction.isActive())
		{
			return invocation.proceed();
		}
		
        transaction.begin();
		boolean rollback = false;
		
        try
        {
        	return invocation.proceed();
        }
        catch(Exception e)
        {
        	transaction.rollback();
        	rollback = true;
        	
        	throw e;
        }
        finally
        {
        	if(false == rollback)
        	{
        		transaction.commit();
        	}
        }
	}

}
