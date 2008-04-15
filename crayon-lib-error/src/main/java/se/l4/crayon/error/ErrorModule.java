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

import com.google.inject.Binder;

import se.l4.crayon.annotation.Description;
import se.l4.crayon.annotation.Module;
import se.l4.crayon.error.internal.ErrorHandlerImpl;
import se.l4.crayon.error.internal.ErrorManagerImpl;

/**
 * Error module, binds implementation of {@link ErrorHandler} and
 * {@link ErrorManager}.
 * 
 * @author Andreas Holstenson
 *
 */
@Module
public class ErrorModule
{
	@Description
	public void bindServices(Binder binder)
	{
		binder.bind(ErrorManager.class).to(ErrorManagerImpl.class);
		binder.bind(ErrorHandler.class).to(ErrorHandlerImpl.class);
	}
}
