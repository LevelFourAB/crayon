/*
 * Copyright 2011 Level Four AB
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
package se.l4.crayon.internal.methods;

import se.l4.crayon.annotation.Order;

/**
 * Callback used for naming of methods, used to provide support for
 * {@link Order} in the {@link MethodResolver}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface MethodResolverCallback
{
	/**
	 * Name the method definition so it can be used in {@link Order}.
	 * 
	 * @param method
	 * 		definition to name
	 * @return
	 * 		name of definition
	 */
	String getName(MethodDef def);
}
