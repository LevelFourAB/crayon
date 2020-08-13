package se.l4.crayon.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	 *   the service to start
	 * @return
	 *   mono that publishes the final state of the service
	 */
	Mono<ServiceStatus> start(Class<? extends ManagedService> service);

	/**
	 * Stop the given service. Stops the service if it is running.
	 *
	 * @param service
	 *   the service to stop
	 * @return
	 *   mono that publishes the final state of the service
	 */
	Mono<ServiceStatus> stop(Class<? extends ManagedService> service);

	/**
	 * Start all services.
	 *
	 * @return
	 *   flux that will publish the final state of all services
	 */
	Flux<ServiceStatus> startAll();

	/**
	 * Stop all services.
	 *
	 * @return
	 *   flux that will publish the final state of all services
	 */
	Flux<ServiceStatus> stopAll();

	/**
	 * Get information about a certain service.
	 *
	 * @param service
	 * 	service to get information for
	 * @return
	 * 	service info
	 */
	Mono<ServiceStatus> get(Class<? extends ManagedService> service);

	/**
	 * Get all of the current services and their status. Will not receive
	 * updates.
	 *
	 * @return
	 */
	Flux<ServiceStatus> services();

	/**
	 * Get a flux that will receive updates to service statuses.
	 *
	 * @return
	 */
	Flux<ServiceStatus> serviceStatus();
}
