package se.l4.crayon.services;

import java.util.EventListener;

/**
 * Service listener interface that can be used to monitor the status of a
 * service.
 */
public interface ServiceListener
	extends EventListener
{
	/**
	 * Status of a service has changed.
	 *
	 * @param info
	 * 		information about service
	 */
	void serviceStatusChanged(ServiceInfo info);
}
