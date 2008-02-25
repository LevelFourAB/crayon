/*
 * Copyright 2008 Andreas Holstenson
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
