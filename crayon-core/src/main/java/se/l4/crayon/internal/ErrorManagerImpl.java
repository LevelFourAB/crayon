package se.l4.crayon.internal;

import java.util.LinkedHashSet;

import com.google.inject.Singleton;

import se.l4.crayon.ErrorHandler;
import se.l4.crayon.ErrorManager;

/**
 * Singleton implementation of the error manager.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ErrorManagerImpl
	implements ErrorManager
{
	private LinkedHashSet<ErrorHandler> handlers;
	
	public ErrorManagerImpl()
	{
		handlers = new LinkedHashSet<ErrorHandler>();
	}
	
	public void addErrorHandler(ErrorHandler handler)
	{
		handlers.add(handler);
	}

	public Iterable<ErrorHandler> getHandlers()
	{
		return handlers;
	}
}
