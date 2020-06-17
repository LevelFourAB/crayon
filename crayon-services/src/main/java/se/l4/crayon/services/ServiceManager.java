package se.l4.crayon.services;

import java.util.Optional;

/**
 * Manager of services, used for starting and stopping system services.
 *
 * <p>
 * Services can be collected via a {@link ServiceContribution} from
 * {@link se.l4.crayon.module.CrayonModule modules}:
 *
 * <pre>
 * {@literal @ServiceContribution}
 * public void contributeService(ServiceCollector collector, SomeService service) {
 *   collector.add(service);
 * }
 * </pre>
 *
 */
public interface ServiceManager
	extends Iterable<ServiceInfo>
{
	/**
	 * Add a service that should be managed.
	 *
	 * @param service
	 */
	void add(ManagedService service);

	/**
	 * Start the given service. Will check if it has already been started
	 * and refuse to start if it has.
	 *
	 * @param service
	 * @throws Exception
	 */
	void start(Class<? extends ManagedService> service)
		throws Exception;

	/**
	 * Stop the given service. Stops the service if it is running.
	 *
	 * @param service
	 * @throws Exception
	 */
	void stop(Class<? extends ManagedService> service)
		throws Exception;

	/**
	 * Start all services.
	 */
	void startAll();

	/**
	 * Stop all services.
	 */
	void stopAll();

	/**
	 * Get information about a certain service.
	 *
	 * @param service
	 * 	service to get information for
	 * @return
	 * 	service info
	 */
	Optional<ServiceInfo> get(Class<? extends ManagedService> service);

	/**
	 * Add a service listener, will from now on be notified of status changes
	 * on all services. Adds a hard reference, disallowing the listener from
	 * being garbage collected.
	 *
	 * @param listener
	 */
	void addListener(ServiceListener listener);

	/**
	 * Remove a service listener, it will no longer be notified of changes
	 * to services.
	 *
	 * @param listener
	 * 		listener to remove
	 */
	void removeListener(ServiceListener listener);
}
