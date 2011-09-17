/*
 * Copyright 2009 Andreas Holstenson
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
package se.l4.crayon.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import se.l4.crayon.ConfigurationException;
import se.l4.crayon.CrayonBinder;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.internal.methods.MethodDef;
import se.l4.crayon.internal.methods.MethodResolver;
import se.l4.crayon.internal.methods.MethodResolverCallback;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Module wrapping any class that is not of type {@link Module} and invokes
 * any methods annotated with {@link Description}.
 * 
 * @author Andreas Holstenson
 *
 */
public class WrapperModule
	implements Module
{
	private final Object delegate;

	public WrapperModule(Object delegate)
	{
		this.delegate = delegate;
	}
	
	public void configure(Binder binder)
	{
		binder = binder.skipSources(WrapperModule.class);
		
		// Ensure that we are bound with our custom binder
		CrayonBinder
			.newBinder(binder)
			.module(delegate);
		
		// Resolve any methods annotated with Description and invoke them
		MethodResolver resolver = new MethodResolver(
			new MethodResolverCallback()
			{
				public String getName(MethodDef def)
				{
					String s = 
						def.getMethod()
							.getAnnotation(Description.class)
							.name();
					
					return "".equals(s) 
						? def.getObject().getClass() + "-" + def.getMethod().getName()
						: s;
				}
			
			},
			Description.class
		);
		
		resolver.add(delegate);
		
		final Set<MethodDef> defs = resolver.getOrder();
		resolver = null;
		
		Object[] params = new Object[] { binder };
		
		// Invoke all of the modules
		for(MethodDef def : defs)
		{
			Method method = def.getMethod();
			Object object = def.getObject();
			
			try
			{
				method.setAccessible(true);
				method.invoke(object, params);
			}
			catch(IllegalArgumentException e)
			{
				throw new ConfigurationException(
					"Module description methods should take a single" 
					+ " argument of type Binder; " + e.getMessage(), e);
			}
			catch(IllegalAccessException e)
			{
				throw new ConfigurationException(e.getMessage(), e);
			}
			catch(InvocationTargetException e)
			{
				Throwable cause = e.getCause();
				
				throw new ConfigurationException(cause.getMessage(), cause);
			}
		}
	}

	@Override
	public String toString()
	{
		return "WrapperModule[" + delegate + "]";
	}
	
	@Override
	public int hashCode()
	{
		return delegate.getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof WrapperModule
			&& ((WrapperModule) obj).delegate.getClass() == delegate.getClass();
	}
}
