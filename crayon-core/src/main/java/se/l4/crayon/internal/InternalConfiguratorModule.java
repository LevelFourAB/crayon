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
package se.l4.crayon.internal;

import com.google.inject.Binder;

import se.l4.crayon.Configurator;
import se.l4.crayon.Environment;
import se.l4.crayon.annotation.Description;

/**
 * Module that is always loaded, containing the base configuration and bindings
 * to support the system. This includes bindings to the {@link Configurator}.
 * 
 * @author Andreas Holstenson
 *
 */
public class InternalConfiguratorModule
{
	private Configurator configurator;
	
	public InternalConfiguratorModule(Configurator configurator)
	{
		this.configurator = configurator;
	}
	
	@Description
	public void configure(Binder binder)
	{
		// Reference to the configurator
		binder.bind(Configurator.class).toInstance(configurator);
		
		// Bind the environment
		Environment env = configurator.getEnvironment();
		binder.bind(Environment.class).toInstance(env);
	}
}
