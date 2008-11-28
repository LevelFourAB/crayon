package se.l4.crayon.osgi;

import org.osgi.framework.ServiceReference;


/**
 * Mapping of a service imported from another OSGi bundle. Contains methods
 * to determine when the service is available and when it is not.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public interface ServiceRef<T>
{
	/**
	 * Get an instance of the service, if unavailable this method will return
	 * {@code null}. When getting a service you are required to call
	 * {@link #unget()} when you are done with it, calling it only once for
	 * every get.
	 * 
	 * @return
	 * 		instance of service if available, otherwise {@code null}
	 */
	T get();
	
	/**
	 * Get a specific instance based on a {@link ServiceReference}.
	 * 
	 * @param ref
	 * 		reference to get instance for
	 * @return
	 */
	T get(ServiceReference ref);
	
	/**
	 * Get all service instances that this reference knows about.
	 * 
	 * @return
	 * 		iterable with all services
	 */
	Iterable<T> getAll();
	
	/**
	 * Get all service references.
	 * 
	 * @return
	 */
	ServiceReference[] getReferences();
	
//	
//	/**
//	 * Unget the service.
//	 */
//	void unget();
	
	/**
	 * Check if the service is currently available.
	 * 
	 * @return
	 */
	boolean isAvailable();
	
	/**
	 * Add a service listener.
	 * 
	 * @param listener
	 */
	void addServiceListener(ServiceListener<T> listener);
	
	/**
	 * Remove a service listener.
	 * 
	 * @param listener
	 */
	void removeServiceListener(ServiceListener<T> listener);
}
