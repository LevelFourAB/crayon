package se.l4.crayon.osgi;

import org.osgi.framework.Filter;

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
	 * Retrieve a reference to the given service using a filter. 
	 * Use this method to get an instance that can be used to get the service 
	 * and track changes to it.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> ServiceRef<T> get(Class<T> type, Filter filter);
	
	/**
	 * Add a service listener that will respond when a certain type is either
	 * available or unavailable. Same as {@link #addServiceListener(Class, Filter, ServiceListener)}
	 * with a {@code null} filter.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		class of type
	 * @param listener
	 * 		listener to use
	 */
	<T> void addServiceListener(Class<T> type, ServiceListener<T> listener);
	
	/**
	 * Add a service listener that will respond when a certain type is either
	 * available or unavailable. This is a shorthand that will call 
	 * {@link #get(Class)} and then {@link ServiceRef#addServiceListener(ServiceListener)}.
	 *  
	 * @param <T>
	 * 		type
	 * @param type
	 * 		class of type
	 * @param filter
	 * 		filter to match against services. {@code null} means match all.
	 * @param listener
	 * 		listener to use
	 */
	<T> void addServiceListener(Class<T> type, Filter filter, ServiceListener<T> listener);
}
