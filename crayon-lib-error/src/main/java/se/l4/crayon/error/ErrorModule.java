package se.l4.crayon.error;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Description;
import se.l4.crayon.error.internal.ErrorHandlerImpl;
import se.l4.crayon.error.internal.ErrorManagerImpl;

/**
 * Error module, binds implementation of {@link ErrorHandler} and
 * {@link ErrorManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ErrorModule
{
	@Description
	public void bindServices(Binder binder)
	{
		binder.bind(ErrorManager.class).to(ErrorManagerImpl.class);
		binder.bind(ErrorHandler.class).to(ErrorHandlerImpl.class);
	}
}
