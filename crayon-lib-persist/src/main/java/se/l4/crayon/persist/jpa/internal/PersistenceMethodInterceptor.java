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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import se.l4.crayon.persist.jpa.JpaHelper;

/**
 * Method interceptor for all methods that should have access to JPA. Will
 * ensure that an {@link EntityManager} is available.
 * 
 * @author Andreas Holstenson
 *
 */
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
