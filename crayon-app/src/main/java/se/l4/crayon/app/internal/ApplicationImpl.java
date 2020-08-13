package se.l4.crayon.app.internal;

import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import se.l4.crayon.app.Application;
import se.l4.crayon.services.ServiceManager;
import se.l4.crayon.services.ServiceStatus;

public class ApplicationImpl
	implements Application
{
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private final Injector injector;
	final ServiceManager services;

	public ApplicationImpl(Injector injector)
	{
		this.injector = injector;
		services = injector.getInstance(ServiceManager.class);

		// Subscribe to updates to service statuses
		services.serviceStatus()
			.subscribe(status -> logger.info(String.format("[ %-8s ] %s", status.getState(), status.getService())));
	}

	@Override
	public Injector getInjector()
	{
		return injector;
	}

	@Override
	public Flux<ServiceStatus> startServices()
	{
		logger.info("Starting all services");
		return services.startAll();
	}

	@Override
	public Flux<ServiceStatus> stopServices()
	{
		logger.info("Stopping all services");
		return services.stopAll();
	}
}
