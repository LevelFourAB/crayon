/*
 * Copyright 2011 Level Four AB
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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;
import se.l4.crayon.services.internal.ServiceManagerImpl;

/**
 * Module configuration for services. Binds {@link ServiceManager} to its
 * default implementation.
 * 
 * @author Andreas Holstenson
 *
 */
public class ServicesModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		// Services
		bind(ServiceManager.class).to(ServiceManagerImpl.class);
	}
	
	@Contribution(name="services")
	@Order("last")
	public void startServices(ServiceManager manager)
	{
		manager.startAll();
		
		Collection<ServiceInfo> info = manager.getInfo();
		if(info.isEmpty())
		{
			return;
		}
		
		Logger logger = LoggerFactory.getLogger(ServiceManager.class);
		logger.info("Service status:");
		
		for(ServiceInfo i : info)
		{
			logger.info(i.toString());
		}
	}
}
