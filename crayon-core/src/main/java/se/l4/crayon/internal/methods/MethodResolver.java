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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.l4.crayon.ConfigurationException;
import se.l4.crayon.annotation.Order;
import se.l4.crayon.internal.DependencyResolver;

/**
 * Class for discovering methods annotated with a special annotation and the
 * order they should be invoked in.
 * 
 * @author Andreas Holstenson
 *
 */
public class MethodResolver
{
	private HashSet<Class<?>> classes;
	
	private Map<String, MethodDef> methods;
	private Map<Class<?>, List<MethodDef>> defs;
	
	private Class<? extends Annotation> annotation;
	private MethodResolverCallback callback;
	
	public MethodResolver(Class<? extends Annotation> annotation,
		MethodResolverCallback callback)
	{
		this.annotation = annotation;
		this.callback = callback;
		
		methods = new HashMap<String, MethodDef>();
		defs = new HashMap<Class<?>, List<MethodDef>>();
		
		classes = new HashSet<Class<?>>();
	}
	
	public void add(Object instance)
	{
		Class<?> type = instance.getClass();
		// Do nothing if the class is already added
		if(false == classes.add(type))
		{
			return;
		}
		
		// Otherwise create an instance and loop through methods
		Method[] declared = type.getMethods();
		List<MethodDef> defs = getMethodDefs(type);
		
		for(Method m : declared)
		{
			if(false == m.isAnnotationPresent(annotation))
			{
				continue;
			}
			
			MethodDef def = new MethodDef(instance, m);
			String name = callback.getName(def);

			// Store the definition
			methods.put(name, def);
			defs.add(def);
		}
	}
	
	private List<MethodDef> getMethodDefs(Class<?> c)
	{
		List<MethodDef> result = defs.get(c);

		if(result == null)
		{
			result = new LinkedList<MethodDef>();
			defs.put(c, result);
		}
		
		return result;
	}
	
	public Set<MethodDef> getOrder()
	{
		DependencyResolver<MethodDef> resolver =
			new DependencyResolver<MethodDef>();

		for(Map.Entry<String, MethodDef> e : methods.entrySet())
		{
			String name = e.getKey();
			MethodDef def = e.getValue();
			
			// Always add without any dependencies
			resolver.add(def);
			
			// Handle dependencies due to method parameters
			for(Class<?> c : def.getMethod().getParameterTypes())
			{
				if(c.isAnnotationPresent(Order.class))
				{
					Order order = c.getAnnotation(Order.class);
					for(String s : order.value())
					{
						if(s.equals(name)) continue;
						
						handleOrderEntry(resolver, def, s);
					}
				}
			}
			
			// Take care of order dependencies
			String[] order = def.getOrder();
			for(String s : order)
			{
				handleOrderEntry(resolver, def, s);
			}
		}
		
		return resolver.getOrder();
	}

	private void handleOrderEntry(DependencyResolver<MethodDef> resolver, MethodDef def, String s)
	{
		if(s.startsWith("before:"))
		{
			s = s.substring(7);
			
			MethodDef d = methods.get(s);
			if(d != null)
			{
				resolver.addDependency(d, def);
			}
		}
		else if(s.startsWith("after:"))
		{
			s = s.substring(6);
			
			MethodDef d = methods.get(s);
			if(d != null)
			{
				resolver.addDependency(def, d);
			}
		}
		else if(s.equals("last"))
		{
			for(MethodDef d : methods.values())
			{
				boolean ok = true;
				for(String sd : d.getOrder())
				{
					if(sd.equals("last"))
					{
						ok = false;
						break;
					}
				}
				
				if(ok)
				{
					resolver.addDependency(def, d);
				}
			}
		}
		else if(s.equals("first"))
		{
			for(MethodDef d : methods.values())
			{
				boolean ok = true;
				for(String sd : d.getOrder())
				{
					if(sd.equals("first"))
					{
						ok = false;
						break;
					}
				}
				
				if(ok)
				{
					resolver.addDependency(d, def);
				}
			}
		}
		else
		{
			throw new ConfigurationException("Invalid order `" + s 
				+ "` in " + def.getMethod().getName() + " (" 
				+ def.getMethod().getDeclaringClass() + ")");
		}
	}
	
}
