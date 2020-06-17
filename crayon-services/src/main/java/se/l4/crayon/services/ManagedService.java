package se.l4.crayon.services;

import java.util.Collections;
import java.util.Set;

/**
 * Managed service that can be started or stopped via {@link ServiceManager}.
 */
public interface ManagedService
{
	/**
	 * Start service.
	 */
	void start(ServiceEncounter encounter)
		throws Exception;

	/**
	 * Stop service.
	 */
	void stop()
		throws Exception;

	/**
	 * Get other services this service depends on.
	 *
	 * @return
	 */
	default Set<Class<? extends ManagedService>> getDependencies()
	{
		return Collections.emptySet();
	}
}
