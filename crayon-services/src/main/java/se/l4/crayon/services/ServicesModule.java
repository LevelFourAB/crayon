package se.l4.crayon.services;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.internal.ServiceManagerImpl;

/**
 * Module configuration for services. Binds {@link ServiceManager} to its
 * default implementation.
 *
 */
public class ServicesModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		bindContributions(ServiceContribution.class);
	}

	@Provides
	@Singleton
	public ServiceManager provideServices(
		@ServiceContribution Contributions contributions
	)
	{
		ServiceManager manager = new ServiceManagerImpl();

		// Run contribution making ServiceCollector available
		contributions.run(binder -> binder.bind(ServiceCollector.class).toInstance(manager::add));

		return manager;
	}
}
