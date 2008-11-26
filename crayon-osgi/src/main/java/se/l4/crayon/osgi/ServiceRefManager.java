package se.l4.crayon.osgi;

/**
 * Manager that helps with getting and tracking services.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServiceRefManager 
{
	/**
	 * Retrieve a reference to the given service. Use this method to get an
	 * instance that can be used to get the service and track changes to it.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> ServiceRef<T> get(Class<T> type);
	
	/**
	 * Add a service listener that will respond when a certain type is either
	 * available or unavailable. This is a shorthand that will call 
	 * {@link #get(Class)} and then {@link ServiceRef#addServiceListener(ServiceListener)}.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		class of type
	 * @param listener
	 * 		listener to use
	 */
	<T> void addServiceListener(Class<T> type, ServiceListener<T> listener);
}
