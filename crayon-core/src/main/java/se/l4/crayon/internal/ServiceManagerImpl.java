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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.crayon.ManagedService;
import se.l4.crayon.ServiceManager;

/**
 * Implementation of {@link ServiceManager}.  
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ServiceManagerImpl
	implements ServiceManager
{
	private static final Logger logger =
		LoggerFactory.getLogger(ServiceManager.class);
	
	private Injector injector;
	private Set<ManagedService> services;
	
	@Inject
	public ServiceManagerImpl(Injector injector)
	{
		services = new CopyOnWriteArraySet<ManagedService>();
		
		this.injector = injector;
		
		// Add a shutdown hook that will close all services
		Runtime.getRuntime().addShutdownHook(
			new Thread() 
			{
				@Override
				public void run()
				{
					ServiceManagerImpl.this.stopAll();
				}
			}
		);
	}
	
	public void addService(ManagedService service)
	{
		services.add(service);
	}
	
	public ManagedService addService(Class<? extends ManagedService> service)
	{
		ManagedService instance = injector.getInstance(service);
		addService(instance);
		
		return instance;
	}
	
	public void startService(ManagedService service)
		throws Exception
	{
		logger.info("Starting service: {}", service);
		
		service.start();
	}

	public void stopService(ManagedService service)
		throws Exception
	{
		logger.info("Stopping service: {}", service);
		
		service.stop();
	}

	public void startAll()
	{		
		for(ManagedService service : services)
		{
			try
			{
				startService(service);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				// FIXME: HANDLE EXCEPTION
			}
		}
	}

	public void stopAll()
	{
		for(ManagedService service : services)
		{
			try
			{
				stopService(service);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				// FIXME: HANDLE EXCEPTION
			}
		}
	}
}
