package se.l4.crayon.services;

public interface ServiceEncounter
{
	/**
	 * Indicate that the service has stopped intentionally.
	 */
	void reportStopped();

	/**
	 * Report failure when running the given service, should <b>only</b> be
	 * used from within a service to update the service manager about its
	 * status.
	 *
	 * @param service
	 * 		service to report failure for
	 */
	void reportFailure(ManagedService service);

	/**
	 * Report failure when running the given service, should <b>only</b> be
	 * used from within a service to update the service manager about its
	 * status.
	 *
	 * @param service
	 * @param e
	 */
	void reportFailure(ManagedService service, Exception e);

}
