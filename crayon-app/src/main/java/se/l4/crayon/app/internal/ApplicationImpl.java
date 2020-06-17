package se.l4.crayon.app.internal;

import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.app.Application;
import se.l4.crayon.services.ServiceManager;

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
		services.addListener(new ApplicationServiceListener());
	}

	@Override
	public Injector getInjector()
	{
		return injector;
	}

	boolean startInitialServices()
	{
		if(services.iterator().hasNext())
		{
			// TODO: This way of figuring out if there are services is hacky at best

			services.startAll();
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void startServices()
	{
		logger.info("Starting all services");
		services.startAll();
	}

	@Override
	public void stopServices()
	{
		logger.info("Stopping all services");
		services.stopAll();
	}
}
