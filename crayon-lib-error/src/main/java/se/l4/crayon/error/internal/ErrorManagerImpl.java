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
package se.l4.crayon.error.internal;

import java.util.LinkedHashSet;

import com.google.inject.Singleton;

import se.l4.crayon.error.ErrorHandler;
import se.l4.crayon.error.ErrorManager;


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
