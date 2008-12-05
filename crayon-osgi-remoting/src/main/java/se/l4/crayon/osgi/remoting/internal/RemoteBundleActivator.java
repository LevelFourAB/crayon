package se.l4.crayon.osgi.remoting.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import se.l4.crayon.osgi.remoting.OSGiRemoteManager;

/**
 * Activator for the remote OSGi support.
 * 
 * @author Andreas Holstenson
 *
 */
public class RemoteBundleActivator
	implements BundleActivator
{
	private ServiceTracker tracker;
	private OSGiRemoteManagerImpl manager;
	
	public RemoteBundleActivator()
	{
	}
	
	public void start(final BundleContext ctx)
		throws Exception
	{
		manager = new OSGiRemoteManagerImpl(ctx);
		manager.start();
		
		Filter filter = ctx.createFilter("(" + OSGiRemoteManager.REMOTE_PUBLISH + "=*)");
		tracker = new ServiceTracker(ctx, filter, new ServiceTrackerCustomizer()
		{

			public Object addingService(ServiceReference ref) 
			{
				Object service = ctx.getService(ref);
				if(service == null)
				{
					// There was no service?
					return null;
				}
				
				manager.registerExported(ref, service);
				
				return service;
			}

			public void modifiedService(ServiceReference ref, Object instance) 
			{
				// TODO: Update the properties of the service
			}

			public void removedService(ServiceReference ref, Object instance) 
			{
				manager.unregisterExported(ref);
				
				ctx.ungetService(ref);
			}
			
		});
		
		ctx.registerService(OSGiRemoteManager.class.getName(), manager, new Hashtable<String, Object>());
		
		tracker.open();
		
		Thread.sleep(1000);
		manager.connect("localhost", OSGiRemoteManager.DEFAULT_PORT);
	}

	public void stop(BundleContext ctx)
		throws Exception
	{
		tracker.close();
		manager.stop();
		
		// TODO: Should we automatically uninstall generated bundles?
	}

}
