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
package se.l4.crayon.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define the order of module configuration and
 * contribution. This annotation takes several string that describe its
 * preferred ordering.
 * 
 * <ul>
 * 	<li>{@code before:name} - define that the method should run before {@code name}</li>
 * 	<li>{@code after:name} - define that the method should run before {@code name}</li>
 *  <li>
 *  	{@code last} - define that a method should run last (depending on other 
 *  	order-definitions)
 *  </li>
 *  <li>
 *  	{@code first} - define that a method should run first (depending on other 
 *  	order-definitions)
 *  </li>
 * </ul>
 * 
 * <p>
 * When the name defined in the order can not be found it is ignored.
 * 
 * @author Andreas Holstenson
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order
{
	String[] value();
}
