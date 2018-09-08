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
import java.util.function.Function;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Function<Method, String> JAVAX_NAME_FINDER = createJavaxInjectNameFinder();

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

	/**
	 * Create a function that can be applied on a method to get any value
	 * stored in javax.inject.Named. This is used as a workaround due to the
	 * Java 9 module system and javax.inject being unlikely to be moduralized.
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Function<Method, String> createJavaxInjectNameFinder()
	{
		try
		{
			Class c = Class.forName("javax.inject.Named");
			Method valueMethod = c.getMethod("value");

			return v -> {
				Annotation a = v.getAnnotation(c);
				
				// No annotation found so no name
				if(a == null) return null;

				// Invoke the value method
				return invoke(valueMethod, a);
			};
		}
		catch(ClassNotFoundException | NoSuchMethodException | SecurityException e)
		{
			return v -> null;
		}
	}

	/**
	 * Invoke the given method and raise a custom exception if unable to
	 * access it on the annotation.
	 * 
	 * @param method
	 *   the method to invoke
	 * @param annotation
	 *   the annotation to invoke on
	 * @return
	 *   the result of the invocation
	 */
	private static String invoke(Method method, Annotation annotation)
	{
		if(method == null) return null;

		try
		{
			return (String) method.invoke(annotation);
		}
		catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Could not access the " + method.getName() + "() of " + annotation + "; " + e.getMessage(), e);
		}
	}

	/**
	 * Get a method from the given annotation.
	 * 
	 * @param annotation
	 *   annotation type to get method from
	 * @param name
	 *   the name of the method
	 */
	private static Method getMethod(Class<? extends Annotation> annotation, String name)
	{
		try
		{
			Method m = annotation.getMethod(name);
			return m.getReturnType() == String.class ? m : null;
		}
		catch(SecurityException | NoSuchMethodException e)
		{
			return null;
		}
	}

	/**
	 * Find the name if annotated with the Guice or javax.inject Named 
	 * annotation.
	 * 
	 * @param def
	 *   the definition of the method
	 */
	private static String findName(MethodDef def)
	{
		Method method = def.getMethod();
		Named named = method.getAnnotation(Named.class);
		if(named != null)
		{
			return named.value();
		}

		return JAVAX_NAME_FINDER.apply(method);
	}

	/**
	 * Create an instance of {@link Contributions} for the given annotation.
	 *
	 * @param annotation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Contributions createContributions(Class<? extends Annotation> annotation)
	{
		Method reflectionName = getMethod(annotation, "name");

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

							String s = findName(def);
							if(s == null)
							{
								// Resolve via the name value on the attribute
								s = invoke(reflectionName, a);
							}
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
					String s = findName(def);
					if(s == null)
					{
						s = def.getMethod()
							.getAnnotation(Contribution.class)
							.name();
					}

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
					String s = findName(def);
					if(s == null)
					{
						s =	def.getMethod()
							.getAnnotation(Shutdown.class)
							.name();
					}

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
