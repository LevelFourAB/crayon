package se.l4.crayon.osgi;

import org.osgi.framework.ServiceReference;

/**
 * Service event, used together with {@link ServiceListener#serviceModified(ServiceRef, ServiceEvent)}
 * to get details about the OSGi actual event.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServiceEvent 
{
	enum Type
	{
		ADDED,
		REMOVED,
		MODIFIED;
	}
	
	Type getType();
	
	ServiceReference getOsgiRef();
	
	boolean preferredChanged();
}
