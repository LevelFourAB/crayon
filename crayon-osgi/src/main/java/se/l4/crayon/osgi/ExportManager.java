package se.l4.crayon.osgi;

import java.util.Map;

import org.osgi.framework.ServiceRegistration;

import com.google.inject.Binder;

/**
 * Manager of exported OSGi services, used for exporting services so they
 * are visible to other bundles. Services that are annotated with {@link Export}
 * and bound will be exported automatically.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ExportManager 
{
	/**
	 * Export the given class.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		interface/class of type. If an interface is given it must be bound
	 * 		via {@link Binder} during the configuration process.
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> ServiceRegistration export(Class<T> type);
	
	/**
	 * Export the given class giving a custom set of attributes.
	 * 
	 * @param <T>
	 * 		type
	 * @param type
	 * 		interface/class of type. If an interface is given it must be bound
	 * 		via {@link Binder} during the configuration process.
	 * @param attributes
	 * 		map with properties
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> ServiceRegistration export(Class<T> type, Map<String, Object> properties);
	
	/**
	 * Short-hand for {@link #export(Class, Map)} that can only take string
	 * attributes.
	 * 
	 * @param <T>
	 * @param type
	 * 		interface/class of type. If an interface is given it must be bound
	 * 		via {@link Binder} during the configuration process.
	 * @param properties
	 * 		array with properties in pairs. Index @{code i} is a key, Index 
	 * 		{@code i+1} is a value.
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> ServiceRegistration export(Class<T> type, String... properties);
	
	/**
	 * Export an object instance, but with zero properties. Otherwise the
	 * same as {@link #export(Object, Map, Class...)}.
	 * 
	 * @param <T>
	 * 		type
	 * @param object
	 * 		object to export
	 * @param types
	 * 		array of types that the service should be exported as
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> ServiceRegistration export(T object, Class<?>... types);
	
	/**
	 * Export an object instance, also defines its properties and the types
	 * it should be export as.
	 * 
	 * @param <T>
	 * 		type
	 * @param object
	 * 		object to export
	 * @param properties
	 * 		properties to export with, {@code null} is treated as no attributes
	 * @param types
	 * 		array of types that the service should be exported as
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> ServiceRegistration export(T object, Map<String, Object> properties, Class<?>... types);
	
	/**
	 * Stop exporting the given class.
	 * 
	 * @param registration
	 * 		registration to remove
	 * @return
	 * 		object that represents the registered service, can be used to
	 * 		update the service properties and to unregister the service.
	 */
	<T> void remove(ServiceRegistration registration);
}
