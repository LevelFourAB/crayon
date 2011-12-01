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
package se.l4.crayon.services.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.ServiceInfo;
import se.l4.crayon.services.ServiceListener;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

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
	
	private Set<ServiceListener> listeners;
	
	private Injector injector;
	private List<ManagedService> services;
	
	private Map<ManagedService, ServiceInfoImpl> status;
	
	@Inject
	public ServiceManagerImpl(Injector injector)
	{
		listeners = new CopyOnWriteArraySet<ServiceListener>();
		
		services = new CopyOnWriteArrayList<ManagedService>();
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
		
		// Add the log listener
		addListener(new LogListener());
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
		
		synchronized(info)
		{
			if(info.status == ServiceStatus.STARTING
				|| info.status == ServiceStatus.RUNNING
				|| info.status == ServiceStatus.STOPPING)
			{
				logger.info("{} has status {}, aborting", service, info.status);
				return;
			}
		
			try
			{
				info.setStatus(ServiceStatus.STARTING);
				
				service.start();
				
				if(info.getStatus() == ServiceStatus.STARTING)
				{
					info.setStatus(ServiceStatus.RUNNING);
				}
			}
			catch(Exception e)
			{
				info.setStatus(ServiceStatus.FAILED, e);
				
				throw e;
			}
		}
	}

	public void stopService(ManagedService service)
		throws Exception
	{
		ServiceInfoImpl info = getInfo(service);
		
		synchronized(info)
		{
			if(info.status == ServiceStatus.FAILED
				|| info.status == ServiceStatus.STARTING
				|| info.status == ServiceStatus.STOPPING
				|| info.status == ServiceStatus.STOPPED)
			{
				logger.info("{} has status {}, aborting", service, info.status);
				return;
			}
			
			try
			{
				info.setStatus(ServiceStatus.STOPPING);
					
				service.stop();

				info.setStatus(ServiceStatus.STOPPED);
			}
			catch(Exception e)
			{
				info.setStatus(ServiceStatus.FAILED, e);
				
				throw e;
			}
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
		for(int i=services.size()-1; i>=0; i--)
		{
			ManagedService service = services.get(i);
			
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
	
	public void reportFailure(ManagedService service)
	{
		ServiceInfoImpl info = getInfo(service);
		info.setStatus(ServiceStatus.FAILED);
	}

	public void reportFailure(ManagedService service, Exception e)
	{
		ServiceInfoImpl info = getInfo(service);
		info.setStatus(ServiceStatus.FAILED, e);
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
			info = new ServiceInfoImpl(service, this);
			status.put(service, info);
		}
		
		return info;
	}
	
	public void addListener(ServiceListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(ServiceListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Inner implementation of {@link ServiceInfo}.
	 *
	 * @author Andreas Holstenson
	 */
	private static class ServiceInfoImpl
		implements ServiceInfo
	{
		private Set<ServiceListener> listeners;
		
		private Exception exception;
		private ManagedService service;
		private ServiceStatus status;
		private ServiceManagerImpl manager;
		
		public ServiceInfoImpl(ManagedService service, ServiceManagerImpl manager)
		{
			listeners = new CopyOnWriteArraySet<ServiceListener>();
			
			this.service = service;
			this.status = ServiceStatus.STOPPED;
			this.exception = null;
			
			this.manager = manager;
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
		
		public void addListener(ServiceListener listener)
		{
			listeners.add(listener);
		}

		public void removeListener(ServiceListener listener)
		{
			listeners.remove(listener);
		}
		
		public void setStatus(ServiceStatus status)
		{
			this.status = status;
			this.exception = null;
			
			fireListeners();
		}
		
		public void setStatus(ServiceStatus status, Exception e)
		{
			this.status = status;
			this.exception = e;
			
			fireListeners();
		}
		
		private void fireListeners()
		{
			for(ServiceListener l : manager.listeners)
			{
				l.serviceStatusChanged(this);
			}
			
			for(ServiceListener l : listeners)
			{
				l.serviceStatusChanged(this);
			}
		}
		
		@Override
		public String toString()
		{
			return String.format("[ %-8s ] %s", getStatus(), getService());
		}
	}

	/**
	 * Internal listener that will log changes to any service registered
	 * with the manager.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class LogListener
		implements ServiceListener
	{
		public void serviceStatusChanged(ServiceInfo info)
		{
			logger.info(info.toString());
		}
	}
}
