package se.l4.crayon.app;

import com.google.inject.Injector;

import se.l4.crayon.app.internal.ApplicationBuilder;
import se.l4.crayon.module.CrayonModule;

public interface Application
{
	/**
	 * Get the injector for the application.
	 */
	Injector getInjector();

	/**
	 * Start all of the {@link se.l4.crayon.services.ManagedService services}
	 * in the system.
	 */
	void startServices();

	/**
	 * Stop all of the  {@link se.l4.crayon.services.ManagedService services}
	 * in the system.
	 */
	void stopServices();

	/**
	 * Start setting up an {@link Application}.
	 *
	 * @param id
	 * @return
	 */
	static Builder withIdentifier(String id)
	{
		return new ApplicationBuilder(id);
	}

	interface Builder
	{
		/**
		 * Add a module to the application.
		 *
		 * @param module
		 * @return
		 */
		Builder add(CrayonModule module);

		/**
		 * Add a new module to the application. This will construct the given
		 * class, assuming it has a default constructor.
		 *
		 * @param module
		 * @return
		 */
		Builder add(Class<? extends CrayonModule> module);

		/**
		 * Start the application.
		 *
		 * @return
		 */
		Application start();
	}
}
