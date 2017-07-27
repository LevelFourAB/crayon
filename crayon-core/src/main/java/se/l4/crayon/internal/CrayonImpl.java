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
package se.l4.crayon.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import se.l4.crayon.ConfigurationException;
import se.l4.crayon.Contribution;
import se.l4.crayon.Contributions;
import se.l4.crayon.Crayon;
import se.l4.crayon.Shutdown;
import se.l4.crayon.internal.methods.MethodDef;
import se.l4.crayon.internal.methods.MethodResolver;
import se.l4.crayon.internal.methods.MethodResolverCallback;

/**
 * Implementation of {@link Crayon}.
 *
 * @author Andreas Holstenson
 *
 */
@Singleton
public class CrayonImpl
	implements Crayon
{
	private static final Logger logger = LoggerFactory.getLogger(CrayonImpl.class);
	private static final Module[] EMPTY = new Module[0];

	private final Set<Object> modules;
	private final Injector injector;

	@Inject
	public CrayonImpl(Injector injector, @Named("crayon-modules") Set<Object> modules)
	{
		this.injector = injector;
		this.modules = modules;
	}

	@Override
	public void start()
	{
		performContributions();
	}

	private static String name(Method method, Annotation annotation)
	{
		if(method == null) return null;

		try
		{
			return (String) method.invoke(annotation);
		}
		catch(IllegalArgumentException e)
		{
		}
		catch(IllegalAccessException e)
		{
		}
		catch(InvocationTargetException e)
		{
		}

		return null;
	}

	private Method getMethod(Class<? extends Annotation> annotation)
	{
		try
		{
			return annotation.getMethod("name");
		}
		catch(SecurityException e)
		{
		}
		catch(NoSuchMethodException e)
		{
		}

		return null;
	}

	/**
	 * Create an instance of {@link Contributions} for the given annotation.
	 *
	 * @param annotation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Contributions createContributions(final Class<? extends Annotation> annotation)
	{
		final Method name = getMethod(annotation);

		return new Contributions()
		{
			@Override
			public void run(Module... modules)
			{
				MethodResolver resolver = new MethodResolver(
					new MethodResolverCallback()
					{
						@Override
						public String getName(MethodDef def)
						{
							Annotation a = def.getMethod()
								.getAnnotation(annotation);

							String s = name(name, a);

							return s == null || "".equals(s)
								? def.getObject().getClass() + "-" + def.getMethod().getName()
								: s;
						}
					},
					annotation
				);

				if(modules.length == 0)
				{
					callMethods(resolver);
				}
				else
				{
					Injector childInjector = injector.createChildInjector(modules);
					callMethods(childInjector, resolver);
				}
			}

			@Override
			public void run()
			{
				run(EMPTY);
			}

			@Override
			public String toString()
			{
				return "Contributions[" + annotation.getSimpleName() + "]";
			}
		};
	}

	/**
	 * Run all contributions on all modules.
	 */
	@SuppressWarnings("unchecked")
	private void performContributions()
	{
		logger.debug("Performing contributions");

		MethodResolver resolver = new MethodResolver(
			new MethodResolverCallback()
			{
				@Override
				public String getName(MethodDef def)
				{
					String s =
						def.getMethod()
							.getAnnotation(Contribution.class)
							.name();

					return "".equals(s)
						? def.getObject().getClass() + "-" + def.getMethod().getName()
						: s;
				}
			},
			Contribution.class
		);

		callMethods(resolver);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void shutdown()
	{
		logger.info("Shutting down");

		MethodResolver resolver = new MethodResolver(
			new MethodResolverCallback()
			{
				@Override
				public String getName(MethodDef def)
				{
					String s =
						def.getMethod()
							.getAnnotation(Shutdown.class)
							.name();

					return "".equals(s)
						? def.getObject().getClass() + "-" + def.getMethod().getName()
						: s;
				}
			},
			Shutdown.class
		);

		callMethods(resolver);
	}

	private void callMethods(MethodResolver resolver)
	{
		callMethods(injector, resolver);
	}

	private void callMethods(Injector injector, MethodResolver resolver)
	{
		for(Object c : modules)
		{
			resolver.add(c);
		}

		final Set<MethodDef> defs = resolver.getOrder();
		resolver = null;

		// run all contributions in order
		for(MethodDef def : defs)
		{
			Method method = def.getMethod();
			Object object = def.getObject();

			Type[] var = method.getGenericParameterTypes();

			Annotation[][] annotations = method.getParameterAnnotations();

			Object[] params = new Object[var.length];

			for(int i=0, n=var.length; i<n; i++)
			{
				// Use the correct method
				Key<?> key = annotations[i].length == 0
					? Key.get(var[i])
					: Key.get(var[i], annotations[i][0]);

				params[i] = injector.getInstance(key);
			}

			try
			{
				method.setAccessible(true);
				method.invoke(object, params);
			}
			catch(IllegalArgumentException e)
			{
				throw new ConfigurationException(e.getMessage(), e);
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
}
