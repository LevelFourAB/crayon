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
package se.l4.crayon.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.annotation.Order;
import se.l4.crayon.services.internal.ServiceManagerImpl;

/**
 * Module configuration for services. Binds {@link ServiceManager} to its
 * default implementation.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServicesModule
{
	@Description
	public void configure(Binder binder)
	{
		// Services
		binder.bind(ServiceManager.class).to(ServiceManagerImpl.class);
	}
	
	@Contribution(name="services")
	@Order("last")
	public void startServices(ServiceManager manager)
	{
		manager.startAll();
		
		Logger logger = LoggerFactory.getLogger(ServiceManager.class);
		logger.info("Service status:");
		
		for(ServiceInfo info : manager.getInfo())
		{
			logger.info(String.format("[ %-7s ] %s", info.getStatus(), info.getService()));
		}
	}
}
