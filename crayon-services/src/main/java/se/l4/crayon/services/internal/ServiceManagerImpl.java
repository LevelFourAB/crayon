package se.l4.crayon.services.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.Iterators;

import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.ServiceEncounter;
import se.l4.crayon.services.ServiceInfo;
import se.l4.crayon.services.ServiceListener;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;
import se.l4.ylem.types.matching.ClassMatchingConcurrentHashMap;
import se.l4.ylem.types.matching.MatchedType;
import se.l4.ylem.types.matching.MutableClassMatchingMap;

/**
 * Implementation of {@link ServiceManager}.
 */
public class ServiceManagerImpl
	implements ServiceManager
{
	private final Set<ServiceListener> listeners;
	private final MutableClassMatchingMap<ManagedService, ServiceInfoImpl> services;

	private volatile boolean needsNewDependencies;

	public ServiceManagerImpl()
	{
		listeners = new CopyOnWriteArraySet<ServiceListener>();

		services = new ClassMatchingConcurrentHashMap<>();
	}

	@Override
	public void add(ManagedService service)
	{
		synchronized(this)
		{
			services.put(service.getClass(), new ServiceInfoImpl(service, this));
			needsNewDependencies = true;
		}
	}

	@Override
	public void start(Class<? extends ManagedService> service)
		throws Exception
	{
		Optional<ServiceInfoImpl> info = services.getBest(service);
		if(! info.isPresent()) return;

		info.get().start();
	}

	@Override
	public void stop(Class<? extends ManagedService> service)
		throws Exception
	{
		Optional<ServiceInfoImpl> info = services.getBest(service);
		if(! info.isPresent()) return;

		info.get().stop();
	}

	@Override
	public void startAll()
	{
		for(MatchedType<?, ServiceInfoImpl> mt : services.entries())
		{
			ServiceInfoImpl info = mt.getData();
			info.start();
		}
	}

	@Override
	public void stopAll()
	{
		for(MatchedType<?, ServiceInfoImpl> mt : services.entries())
		{
			ServiceInfoImpl info = mt.getData();
			info.stop();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Optional<ServiceInfo> get(Class<? extends ManagedService> service)
	{
		return (Optional) services.getBest(service);
	}

	@Override
	public Iterator<ServiceInfo> iterator()
	{
		return Iterators.transform(
			services.entries().iterator(),
			s -> s.getData()
		);
	}

	@Override
	public void addListener(ServiceListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(ServiceListener listener)
	{
		listeners.remove(listener);
	}

	private void maybeRecalculateDependencies()
	{
		synchronized(this)
		{
			if(! needsNewDependencies) return;

			// Clear dependencies
			for(MatchedType<?, ServiceInfoImpl> mt : services.entries())
			{
				ServiceInfoImpl info = mt.getData();
				info.incomingDependencies.clear();
				info.outgoingDependencies.clear();
			}

			// Copy dependencies
			for(MatchedType<?, ServiceInfoImpl> mt : services.entries())
			{
				ServiceInfoImpl info = mt.getData();

				for(Class<? extends ManagedService> c : info.service.getDependencies())
				{
					Optional<ServiceInfoImpl> service = services.getBest(c);
					if(! service.isPresent())
					{
						// TODO: What do we do if the service doesn't exist?
						continue;
					}

					info.outgoingDependencies.add(service.get());
					service.get().incomingDependencies.add(info);
				}
			}

			needsNewDependencies = false;
		}
	}

	/**
	 * Inner implementation of {@link ServiceInfo}.
	 */
	private static class ServiceInfoImpl
		implements ServiceInfo, ServiceEncounter
	{
		private Set<ServiceListener> listeners;

		private Throwable exception;
		private ManagedService service;
		private ServiceStatus status;
		private ServiceManagerImpl manager;

		private final Set<ServiceInfo> incomingDependencies;
		private final Set<ServiceInfo> outgoingDependencies;

		public ServiceInfoImpl(ManagedService service, ServiceManagerImpl manager)
		{
			listeners = new CopyOnWriteArraySet<ServiceListener>();

			this.service = service;
			this.status = ServiceStatus.STOPPED;
			this.exception = null;

			this.manager = manager;
			this.outgoingDependencies = new HashSet<>();
			this.incomingDependencies = new HashSet<>();
		}

		@Override
		public Optional<Throwable> getFailedWith()
		{
			return Optional.ofNullable(exception);
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

		public void setStatus(ServiceStatus status, Throwable e)
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
		public synchronized void start()
		{
			if(status == ServiceStatus.STARTING
				|| status == ServiceStatus.RUNNING
				|| status == ServiceStatus.STOPPING)
			{
				return;
			}

			manager.maybeRecalculateDependencies();

			try
			{
				setStatus(ServiceStatus.STARTING);

				// Make sure that the services we depend on are started
				for(ServiceInfo dependency : outgoingDependencies)
				{
					dependency.start();

					if(dependency.getStatus() != ServiceStatus.RUNNING)
					{
						setStatus(ServiceStatus.FAILED);
						return;
					}
				}

				// Start our own service
				service.start(this);

				if(getStatus() == ServiceStatus.STARTING)
				{
					// Update the status if the job didn't update status on its own
					setStatus(ServiceStatus.RUNNING);
				}
			}
			catch(Throwable e)
			{
				setStatus(ServiceStatus.FAILED, e);
			}
		}

		@Override
		public void stop()
		{
			if(status == ServiceStatus.FAILED
				|| status == ServiceStatus.STARTING
				|| status == ServiceStatus.STOPPING
				|| status == ServiceStatus.STOPPED)
			{
				return;
			}

			manager.maybeRecalculateDependencies();

			try
			{
				setStatus(ServiceStatus.STOPPING);

				// Make sure that the services that depend on us are stopped
				for(ServiceInfo dependency : incomingDependencies)
				{
					dependency.stop();

					if(dependency.getStatus() != ServiceStatus.STOPPED
						&& dependency.getStatus() != ServiceStatus.FAILED)
					{
						setStatus(ServiceStatus.FAILED);
						return;
					}
				}

				service.stop();

				setStatus(ServiceStatus.STOPPED);
			}
			catch(Exception e)
			{
				setStatus(ServiceStatus.FAILED, e);
			}
		}

		@Override
		public void reportStopped()
		{
			setStatus(ServiceStatus.STOPPED);
		}

		@Override
		public void reportFailure(ManagedService service)
		{
			setStatus(ServiceStatus.FAILED);
		}

		@Override
		public void reportFailure(ManagedService service, Exception e)
		{
			this.exception = e;
			setStatus(ServiceStatus.FAILED);
		}

		@Override
		public String toString()
		{
			return String.format("[ %-8s ] %s", getStatus(), getService());
		}
	}
}
