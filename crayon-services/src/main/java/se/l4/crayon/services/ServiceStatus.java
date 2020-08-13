package se.l4.crayon.services;

import java.util.Optional;

/**
 * Status of a service within the system.
 */
public interface ServiceStatus
{
	enum State
	{
		/** Service is stopping. */
		STOPPING,

		/** Service is stopped. */
		STOPPED,

		/** Service is starting. */
		STARTING,

		/** Service is running. */
		RUNNING,

		/** Service failed to start. */
		FAILED
	}

	/**
	 * Get instance that the service information is for.
	 *
	 * @return
	 *   instance of {@link ManagedService}
	 */
	ManagedService getService();

	/**
	 * Get the state of the service.
	 *
	 * @return
	 *   service stat
	 */
	State getState();

	/**
	 * Get the exception the service failed with, if {@link #getState()}
	 * returns {@link ServiceStatus#FAILED}.
	 *
	 * @return
	 *   exception that it failed with, or {@code null} if no failure
	 */
	Optional<Throwable> getFailedWith();
}
