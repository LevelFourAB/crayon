package se.l4.crayon.services.internal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import se.l4.crayon.services.ManagedService;
import se.l4.crayon.services.RunningService;
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
	private final MutableClassMatchingMap<ManagedService, Service> services;
	private volatile boolean needsNewDependencies;

	private final ReplayProcessor<ServiceStatus> events;
	private final FluxSink<ServiceStatus> eventsSink;

	public ServiceManagerImpl()
	{
		services = new ClassMatchingConcurrentHashMap<>();

		events = ReplayProcessor.create(0);
		eventsSink = events.sink();
	}

	@Override
	public void add(ManagedService service)
	{
		synchronized(this)
		{
			services.put(service.getClass(), new Service(service, this));
			needsNewDependencies = true;
		}
	}

	@Override
	public Mono<ServiceStatus> start(Class<? extends ManagedService> service)
	{
		return Mono.defer(() -> {
			Optional<Service> info = services.getBest(service);
			if(! info.isPresent()) return Mono.empty();

			return info.get().start();
		});
	}

	@Override
	public Mono<ServiceStatus> stop(Class<? extends ManagedService> service)
	{
		return Mono.defer(() -> {
			Optional<Service> info = services.getBest(service);
			if(! info.isPresent()) return Mono.empty();

			return info.get().stop();
		});
	}

	@Override
	public Flux<ServiceStatus> startAll()
	{
		return Flux.fromIterable(services.entries())
			.flatMap(mt -> mt.getData().start());
	}

	@Override
	public Flux<ServiceStatus> stopAll()
	{
		return Flux.fromIterable(services.entries())
			.flatMap(mt -> mt.getData().stop());
	}

	@Override
	public Mono<ServiceStatus> get(Class<? extends ManagedService> service)
	{
		return Mono.fromSupplier(() -> services.getBest(service).orElse(null))
			.map(s -> s.currentStatus);
	}

	@Override
	public Flux<ServiceStatus> serviceStatus()
	{
		return events;
	}

	@Override
	public Flux<ServiceStatus> services()
	{
		return Flux.fromIterable(services.entries())
			.map(service -> service.getData().currentStatus);
	}

	private void maybeRecalculateDependencies()
	{
		synchronized(this)
		{
			if(! needsNewDependencies) return;

			// Clear dependencies
			for(MatchedType<?, Service> mt : services.entries())
			{
				Service info = mt.getData();
				info.incomingDependencies.clear();
				info.outgoingDependencies.clear();
			}

			// Copy dependencies
			for(MatchedType<?, Service> mt : services.entries())
			{
				Service info = mt.getData();

				for(Class<? extends ManagedService> c : info.service.getDependencies())
				{
					Optional<Service> service = services.getBest(c);
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
	 * Inner implementation of {@link ServiceStatus}.
	 */
	private static class Service
	{
		private final ManagedService service;
		private final ServiceManagerImpl manager;

		private ServiceStatus currentStatus;
		private Mono<ServiceStatus> currentChange;
		private RunningService runningService;

		private final Set<Service> incomingDependencies;
		private final Set<Service> outgoingDependencies;

		public Service(ManagedService service, ServiceManagerImpl manager)
		{
			this.service = service;

			this.manager = manager;

			currentStatus = new ServiceStatusImpl(this, ServiceStatus.State.STOPPED, null);

			this.outgoingDependencies = new HashSet<>();
			this.incomingDependencies = new HashSet<>();
		}

		private ServiceStatus switchState(ServiceStatus.State state, Throwable failedWith)
		{
			currentStatus = new ServiceStatusImpl(this, state, failedWith);
			manager.eventsSink.next(currentStatus);
			return currentStatus;
		}

		public Mono<ServiceStatus> start()
		{
			return Mono.defer(() -> {
				ServiceStatus.State state = currentStatus.getState();
				if(state == ServiceStatus.State.RUNNING)
				{
					return Mono.just(currentStatus);
				}

				if(state == ServiceStatus.State.STOPPING)
				{
					return Mono.error(new RuntimeException("Unable to start service while it is being stopped"));
				}

				if(state == ServiceStatus.State.STARTING)
				{
					return currentChange;
				}

				manager.maybeRecalculateDependencies();

				switchState(ServiceStatus.State.STARTING, null);

				return currentChange = Flux.fromIterable(outgoingDependencies)
					.flatMap(d -> d.start())
					.reduce(ServiceStatus.State.RUNNING, (a, b) -> b.getState() == ServiceStatus.State.RUNNING ? a : ServiceStatus.State.FAILED)
					.flatMap(status -> {
						if(status == ServiceStatus.State.RUNNING)
						{
							// All dependencies could start, so let's start ourself
							return service.start()
								.map(rs -> {
									this.runningService = rs;

									return switchState(ServiceStatus.State.RUNNING, null);
								});
						}
						else
						{
							// One or more dependencies failed
							return Mono.just(switchState(
								ServiceStatus.State.FAILED,
								new RuntimeException("Could not start due to not all dependencies starting")
							));
						}
					})
					.onErrorResume(t -> {
						return Mono.just(switchState(ServiceStatus.State.FAILED, t));
					})
					.cache();
			});
		}

		public Mono<ServiceStatus> stop()
		{
			return Mono.defer(() -> {
				ServiceStatus.State state = currentStatus.getState();
				if(state == ServiceStatus.State.STOPPED)
				{
					return Mono.just(currentStatus);
				}

				if(state == ServiceStatus.State.FAILED)
				{
					return Mono.just(switchState(ServiceStatus.State.STOPPED, null));
				}

				if(state == ServiceStatus.State.STARTING)
				{
					return Mono.error(new RuntimeException("Service is currently being started, can't stop"));
				}

				if(state == ServiceStatus.State.STOPPING)
				{
					return currentChange;
				}

				manager.maybeRecalculateDependencies();

				switchState(ServiceStatus.State.STOPPING, null);

				return currentChange = Flux.fromIterable(incomingDependencies)
					.flatMap(d -> d.stop())
					.reduce(ServiceStatus.State.STOPPED, (a, b) -> b.getState() == ServiceStatus.State.STOPPED ? a : ServiceStatus.State.RUNNING)
					.flatMap(status -> {
						if(status == ServiceStatus.State.STOPPED)
						{
							// Everything depending on this service has stopped
							return runningService.stop()
								.map(v -> {
									return switchState(ServiceStatus.State.STOPPED, null);
								});
						}
						else
						{
							// One or more things depending on us didn't stop - keep running
							return Mono.just(currentStatus);
						}
					})
					.onErrorResume(t -> {
						return Mono.just(switchState(ServiceStatus.State.FAILED, t));
					})
					.cache();
			});
		}
	}

	private static class ServiceStatusImpl
		implements ServiceStatus
	{
		private final Service service;
		private final ServiceStatus.State state;
		private final Throwable failedWith;

		public ServiceStatusImpl(
			Service service,
			ServiceStatus.State state,
			Throwable failedWith
		)
		{
			this.service = service;
			this.state = state;
			this.failedWith = failedWith;
		}

		@Override
		public ManagedService getService()
		{
			return service.service;
		}

		@Override
		public ServiceStatus.State getState()
		{
			return state;
		}

		@Override
		public Optional<Throwable> getFailedWith()
		{
			return Optional.of(failedWith);
		}
	}
}
