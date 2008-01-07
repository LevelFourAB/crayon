package se.l4.crayon.osgi.internal;

import org.osgi.framework.BundleContext;

import com.google.inject.Binder;

import se.l4.crayon.annotation.ModuleDescription;

public class OSGiModule
{
	private BundleContext context;
	
	public OSGiModule(BundleContext context)
	{
		this.context = context;
	}
	
	@ModuleDescription
	public void configure(Binder binder)
	{
		binder.bind(BundleContext.class).toInstance(context);
	}
}
