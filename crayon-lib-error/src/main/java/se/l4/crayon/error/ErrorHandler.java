package se.l4.crayon.error;

import com.google.inject.Inject;

/**
 * Error handler, used for handling of exceptions/throwables that can not be
 * handled in a good way. Usable in two ways, either injected ({@link Inject})
 * or implemented and contributed to {@link ErrorManager}.
 * 
 * <p>
 * Example usage:
 * <pre>
 * public void MyClass {
 * 		private ErrorHandler errorHandler;
 * 
 * 		{@literal @Inject}
 * 		public void MyClass(ErrorHandler errorHandler) {
 * 			this.errorHandler = errorHandler;
 * 		}
 * 
 * 		public void doSomething() throws Exception {
 * 			... do something ...
 * 		}
 * 
 * 		// let the error handler handle exceptions
 * 		public void doSomething2() {
 * 			try {
 * 				doSomething();
 * 			}
 * 			catch(Exception e) {
 * 				errorHandler.handle(e);
 * 			}
 * 		}
 * }
 * </pre>
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
