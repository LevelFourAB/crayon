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

import com.google.inject.Provider;

/**
 * Small provider that returns the current {@link EntityManager}.
 * 
 * @author Andreas Holstenson
 *
 */
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
