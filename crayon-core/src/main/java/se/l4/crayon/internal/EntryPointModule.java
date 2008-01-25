package se.l4.crayon.internal;

import com.google.inject.Binder;

import se.l4.crayon.Configurator;
import se.l4.crayon.ServiceManager;
import se.l4.crayon.annotation.Description;

/**
 * Module that is always loaded, containing the base configuration and bindings
 * to support the system. This includes bindings to the {@link Configurator} and
 * {@link ServiceManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class EntryPointModule
{
	private Configurator entryPoint;
	
	public EntryPointModule(Configurator entryPoint)
	{
		this.entryPoint = entryPoint;
		
	}
	
	@Description
	public void configure(Binder binder)
	{
		// Reference to entry point
		binder.bind(Configurator.class).toInstance(entryPoint);
		
		// Services
		binder.bind(ServiceManager.class).to(ServiceManagerImpl.class);
	}
	
}
