package se.l4.crayon;

/**
 * Error handler, used for handling of exceptions/throwables that can not be
 * handled in a good way.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ErrorHandler
{
	/**
	 * Handle the given throwable.
	 * 
	 * @param t
	 */
	void handle(Throwable t);
	
	/**
	 * Handle the given throwable, including a more detailed message.
	 * 
	 * @param message
	 * @param t
	 */
	void handle(String message, Throwable t);
}
