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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.l4.crayon.After;
import se.l4.crayon.Before;
import se.l4.crayon.Order;

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
	
	public List<String> getOrder()
	{
		List<String> order = new ArrayList<String>();
		handle(method.getAnnotations(), order, new HashSet<Class<? extends Annotation>>());
		return order;
	}
	
	private void handle(Annotation[] annotations, List<String> order, Set<Class<? extends Annotation>> visited)
	{
		for(Annotation a : annotations)
		{
			if(a instanceof Order)
			{
				order.addAll(Arrays.asList(((Order) a).value()));
			}
			else if(a instanceof After)
			{
				for(String s : ((After) a).value())
				{
					order.add("after:" + s);
				}
			}
			else if(a instanceof Before)
			{
				for(String s : ((Before) a).value())
				{
					order.add("before:" + s);
				}
			}
			else
			{
				if(visited.add(a.annotationType()))
				{
					handle(a.annotationType().getAnnotations(), order, visited);
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return method.getName() + " (" + getObject().getClass() + ")";
	}
}
