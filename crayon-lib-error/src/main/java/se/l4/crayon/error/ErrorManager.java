package se.l4.crayon.error;


/**
 * Manager of error handlers, used to define which error handlers should
 * be used in the system.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ErrorManager
{
	void addErrorHandler(ErrorHandler handler);
}
