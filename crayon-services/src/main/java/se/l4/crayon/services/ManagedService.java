package se.l4.crayon.services;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import reactor.core.publisher.Mono;

/**
 * Managed service that can be started or stopped via {@link ServiceManager}.
 */
public interface ManagedService
{
	/**
	 * Start service.
	 */
	Mono<RunningService> start();

	/**
	 * Get other services this service depends on.
	 *
	 * @return
	 */
	default ImmutableSet<Class<? extends ManagedService>> getDependencies()
	{
		return Sets.immutable.empty();
	}
}
