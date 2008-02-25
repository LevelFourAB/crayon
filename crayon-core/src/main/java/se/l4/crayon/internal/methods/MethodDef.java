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
package se.l4.crayon.internal.methods;

import java.lang.reflect.Method;

import se.l4.crayon.annotation.Order;

public class MethodDef
{
	private final Object object;
	private final Method method;
	
	public MethodDef(Object object, Method method)
	{
		this.object = object;
		this.method = method;
	}
	
	public Method getMethod()
	{
		return method;
	}
	
	public Object getObject()
	{
		return object;
	}
	
	public String[] getOrder()
	{
		Order order = method.getAnnotation(Order.class);
		
		return order == null ? new String[0] : order.value();
	}
	
	@Override
	public String toString()
	{
		return method.getName() + " (" + getObject().getClass() + ")";
	}
}
