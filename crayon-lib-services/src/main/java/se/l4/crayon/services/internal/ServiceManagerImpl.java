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
package se.l4.crayon.services.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.ServiceInfo;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;

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
	
	private Map<ManagedService, ServiceInfoImpl> status;
	
	@Inject
	public ServiceManagerImpl(Injector injector)
	{
		services = new CopyOnWriteArraySet<ManagedService>();
		status = new ConcurrentHashMap<ManagedService, ServiceInfoImpl>();
		
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
		
		ServiceInfoImpl info = getInfo(service);
		info.status = ServiceStatus.STOPPED;
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
		ServiceInfoImpl info = getInfo(service);
		
		try
		{
			info.status = ServiceStatus.STARTING;
			log(info);
			
			service.start();
			
			info.status = ServiceStatus.RUNNING;
			log(info);
		}
		catch(Exception e)
		{
			info.exception = e;
			info.status = ServiceStatus.FAILED;
			log(info);
			
			throw e;
		}
	}

	public void stopService(ManagedService service)
		throws Exception
	{
		ServiceInfoImpl info = getInfo(service);
		
		try
		{
			if(info.status == ServiceStatus.RUNNING)
			{
				info.status = ServiceStatus.STOPPING;
				log(info);
				
				service.stop();

				info.status = ServiceStatus.STOPPED;
				log(info);
			}
		}
		catch(Exception e)
		{
			info.exception = e;
			info.status = ServiceStatus.FAILED;
			log(info);
			
			throw e;
		}
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
				logger.warn("Failed to start: " + service + "; " + e.getMessage(), e);
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
				logger.warn("Failed to stop: " + service + "; " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<ServiceInfo> getInfo()
	{
		return (Collection) status.values();
	}

	public ServiceInfoImpl getInfo(ManagedService service)
	{
		ServiceInfoImpl info = status.get(service);
		if(info == null)
		{
			info = new ServiceInfoImpl(service);
			status.put(service, info);
		}
		
		return info;
	}
	
	private void log(ServiceInfo info)
	{
		logger.info(info.toString());
	}
	
	private static class ServiceInfoImpl
		implements ServiceInfo
	{
		private Exception exception;
		private ManagedService service;
		private ServiceStatus status;
		
		public ServiceInfoImpl(ManagedService service)
		{
			this.service = service;
			this.status = ServiceStatus.STOPPED;
			this.exception = null;
		}
		
		public Exception getFailedWith()
		{
			return exception;
		}

		public ManagedService getService()
		{
			return service;
		}

		public ServiceStatus getStatus()
		{
			return status;
		}
		
		@Override
		public String toString()
		{
			return String.format("[ %-8s ] %s", getStatus(), getService());
		}
	}
}
