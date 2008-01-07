package se.l4.crayon.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import se.l4.crayon.EntryPoint;
import se.l4.crayon.osgi.internal.OSGiModule;

public class CrayonBundleActivator
	implements BundleActivator
{

	public final void start(BundleContext context)
		throws Exception
	{
		// Create entry point and request it to be configured
		EntryPoint ep = new EntryPoint();
		ep.addInstance(new OSGiModule(context));
		ep.addInstance(this);
		
		configureEntryPoint(ep);
		
		// Configure and contribute
		ep.start();
	}

	public void stop(BundleContext context)
		throws Exception
	{
		
	}
	
	protected void configureEntryPoint(EntryPoint point)
	{
	}
}
