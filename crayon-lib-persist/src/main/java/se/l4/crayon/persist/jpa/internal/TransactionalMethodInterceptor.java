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

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import se.l4.crayon.persist.Transactional;

/**
 * Method interceptor for {@link Transactional} methods, will wrap the calls
 * with the creation and commit/rollback of a transaction.
 * 
 * @author Andreas Holstenson
 *
 */
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
        catch(Throwable t)
        {
        	if(shouldRollback(invocation.getMethod(), t))
			{
				transaction.rollback();
				rollback = true;
			}
        	
        	throw t;
        }
        finally
        {
        	if(false == rollback)
        	{
        		transaction.commit();
        	}
        }
	}

	private boolean shouldRollback(Method m, Throwable t)
	{
		Transactional md = m.getAnnotation(Transactional.class);
		for(Class<? extends Throwable> t1 : md.rollbackOn())
		{
			if(false == t.getClass().isAssignableFrom(t1))
			{
				return false;
			}
		}
		
		return true;
	}
}
