package se.l4.crayon.internal;

import com.google.inject.Binder;

import se.l4.crayon.EntryPoint;
import se.l4.crayon.ErrorHandler;
import se.l4.crayon.ErrorManager;
import se.l4.crayon.ServiceManager;
import se.l4.crayon.annotation.Description;

/**
 * Module that is always loaded, containing the base configuration and bindings
 * to support the system. This includes bindings to the {@link EntryPoint} and
 * {@link ServiceManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class EntryPointModule
{
	private EntryPoint entryPoint;
	
	public EntryPointModule(EntryPoint entryPoint)
	{
		this.entryPoint = entryPoint;
		
	}
	
	@Description
	public void configure(Binder binder)
	{
		// Reference to entry point
		binder.bind(EntryPoint.class).toInstance(entryPoint);
		
		// Services
		binder.bind(ServiceManager.class).to(ServiceManagerImpl.class);
		
		// Error handling
		binder.bind(ErrorManager.class).to(ErrorManagerImpl.class);
		binder.bind(ErrorHandler.class).to(ErrorHandlerImpl.class);
	}
	
}
