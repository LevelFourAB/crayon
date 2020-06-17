package se.l4.crayon.app.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.services.ServiceInfo;
import se.l4.crayon.services.ServiceListener;
import se.l4.crayon.services.ServiceManager;

/**
 * Listener that outputs information about service status as it changes.
 */
public class ApplicationServiceListener
	implements ServiceListener
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

	@Override
	public void serviceStatusChanged(ServiceInfo info)
	{
		logger.info(toString(info));
	}

	public static String toString(ServiceInfo info)
	{
		return String.format("[ %-8s ] %s", info.getStatus(), info.getService());
	}
}
