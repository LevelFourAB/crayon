package se.l4.crayon.osgi;

/**
 * Listener used to determine when a service becomes available or is made
 * unavailable.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface ServiceListener<T>
{
	/**
	 * The given service has become available.
	 * 
	 * @param <T>
	 * @param service
	 */
	void serviceAvailable(ServiceRef<T> service);
	
	/**
	 * The given service has become unavailable.
	 * 
	 * @param <T>
	 * @param service
	 */
	void serviceUnavailable(ServiceRef<T> service);
	
	/**
	 * The service has been modified, either more or less instances are 
	 * available or the preferred variant has changed.
	 * 
	 * @param service
	 */
	void serviceModified(ServiceRef<T> service, ServiceEvent event);
}
