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
import javax.persistence.PersistenceContext;

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;

import se.l4.crayon.annotation.Description;
import se.l4.crayon.annotation.Module;
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
@Module
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
