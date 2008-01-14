package se.l4.crayon.error.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.crayon.error.ErrorHandler;


/**
 * Implementation of {@link ErrorHandler} that acts as a facade to the real
 * error handlers.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ErrorHandlerImpl
	implements ErrorHandler
{
	private ErrorManagerImpl manager;
	
	@Inject
	public ErrorHandlerImpl(ErrorManagerImpl manager)
	{
		this.manager = manager;
	}
	
	public void handle(Throwable t)
	{
		for(ErrorHandler e : manager.getHandlers())
		{
			e.handle(t);
		}
	}

	public void handle(String message, Throwable t)
	{
		for(ErrorHandler e : manager.getHandlers())
		{
			e.handle(message, t);
		}
	}
	
}
