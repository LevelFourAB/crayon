package se.l4.crayon.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import se.l4.crayon.Configurator;
import se.l4.crayon.osgi.internal.InternalServices;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Configurator class used for OSGi bundles. Usage is the same as with
 * {@link Configurator}.
 * 
 * @author Andreas Holstenson
 *
 */
public class OSGiConfigurator
{
	private Configurator configurator;
	
	public OSGiConfigurator(final BundleContext ctx)
	{
		configurator = new Configurator();
		configurator.setLogger(LoggerFactory.getLogger(getClass().getName() + " [" + ctx.getBundle() + "]"));
		
		configurator.addInstance(new InternalServices(ctx));
	}
	
	public OSGiConfigurator(BundleContext ctx, Object description)
	{
		this(ctx);
		
		configurator.addInstance(description);
	}
	
	public OSGiConfigurator(BundleContext ctx, Class<?> description)
	{
		this(ctx);
		
		configurator.add(description);
	}

	public OSGiConfigurator add(Class<?> type)
	{
		configurator.add(type);
		
		return this;
	}

	public OSGiConfigurator addGuiceModule(Class<? extends Module> type)
	{
		configurator.addGuiceModule(type);
		
		return this;
	}

	public OSGiConfigurator addGuiceModule(Module module)
	{
		configurator.addGuiceModule(module);
		
		return this;
	}

	public OSGiConfigurator addInstance(Object instance)
	{
		configurator.addInstance(instance);
		
		return this;
	}

	public void configure()
	{
		configurator.configure();
	}
	
	public void shutdown()
	{
		configurator.shutdown();
	}

	public Injector getInjector()
	{
		return configurator.getInjector();
	}
}
