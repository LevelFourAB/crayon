package se.l4.crayon.services;

import java.util.Optional;

/**
 * Information about a service in the system, obtained via
 * {@link ServiceManager#getInfo(ManagedService)} or
 * {@link ServiceManager#getInfo()}. The same service will always point to
 * the same {@code ServiceInfo}.
 */
public interface ServiceInfo
{
	/**
	 * Get instance that the service information is for.
	 *
	 * @return
	 *   instance of {@link ManagedService}
	 */
	ManagedService getService();

	/**
	 * Get the status of the service.
	 *
	 * @return
	 *   service status
	 */
	ServiceStatus getStatus();

	/**
	 * Get the exception the service failed with, if {@link #getStatus()}
	 * returns {@link ServiceStatus#FAILED}.
	 *
	 * @return
	 *   exception that it failed with, or {@code null} if no failure
	 */
	Optional<Throwable> getFailedWith();

	/**
	 * Add a service listener, will from now on be notified of status changes
	 * in the service. Adds a hard reference, disallowing the listener from
	 * being garbage collected.
	 *
	 * @param listener
	 */
	void addListener(ServiceListener listener);

	/**
	 * Remove a service listener, it will no longer be notified of changes
	 * to the service this object represents.
	 *
	 * @param listener
	 * 		listener to remove
	 */
	void removeListener(ServiceListener listener);

	void start();

	void stop();
}
