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
package se.l4.crayon;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Dependencies;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.internal.ClassLocator;
import se.l4.crayon.internal.EntryPointModule;
import se.l4.crayon.internal.methods.MethodDef;
import se.l4.crayon.internal.methods.MethodResolver;
import se.l4.crayon.internal.methods.MethodResolverCallback;

/**
 * Entry point for system, used for defining which modules should be used and
 * what services that should be started. Modules need to implement
 * {@link Module}, or extend {@link AbstractModule}. See 
 * <a href="http://code.google.com/p/google-guice/">Guice website</a> for more
 * information about modules.
 * 
 * <h2>Auto-discovery of modules</h2>
 * The entry point can attempt to discover modules stored in Jar-files. This
 * is done by looking at the manifest of the Jar-file. Storing a key inside
 * it named {@code System-Modules} (see {@link #MANIFEST_KEY}). The key should
 * contain a value with fully qualified class names separated with commas (,).
 * Those classes will be loaded and configured by the entry point.
 * 
 * <p>
 * Discovery is done via the method {@link #discover()}.
 * 
 * <h2>Contribution support</h2>
 * Each modules supports contribution of properties or configuration of objects.
 * This is done via automatic invocation of methods annotated with 
 * {@link Contribution}. Such methods will be run after the Guice-specific 
 * configuration has been run. Any arguments of the methods will be given 
 * values via injection.
 * 
 * <h2>Services</h2>
 * The entry point will request that all defined {@link ManagedService}s are to
 * be started. To define such services define a contribution in your module.
 * 
 * <pre>
 * {@literal @Contribution}
 * public void contributeServices(ServiceManager manager) {
 * 	manager.addService(MyService.class);
 * }
 * </pre>
 * 
 * <h2>Logging</h2>
 * The entry point supports configuration of a logging system of your choice.
 * This is done via binding an implementation to {@link LoggingConfigurator}.
 * After all modules have been configured, the configurator for logging will
 * be retrieved and run. After this all contributions from modules will be made.
 * Due to this logging in modules can only be done in those methods that are
 * run as part of the contribution process.
 * 
 * @author Andreas Holstenson
 *
 */
public class Configurator
{
	public static final String MANIFEST_KEY = "System-Modules";
	
	private static final Logger logger = 
		LoggerFactory.getLogger(Configurator.class);
	
	private Injector configurationInjector;
	
	private Map<Class<?>, Object> moduleInstances;
	private Set<Class<?>> modules;

	private List<Module> guiceModules;
	
	private Injector injector;
	
	private EntryPointModule entryPointModule;
	
	public Configurator()
	{
		configurationInjector = Guice.createInjector();
	
		modules = new HashSet<Class<?>>();
		moduleInstances = new HashMap<Class<?>, Object>();
		guiceModules = new LinkedList<Module>();
		
		// add default module
		modules.add(EntryPointModule.class);
		entryPointModule = new EntryPointModule(this);
		addInstance(entryPointModule);
	}
	
	/**
	 * Attempt to discover modules stored in Jar-files and add them to the
	 * configuration. Will use {@link #MANIFEST_KEY} for search.
	 * 
	 * @return
	 * 		self
	 */
	public Configurator discover()
	{
		return discover(MANIFEST_KEY);
	}
	
	/**
	 * Attempt to discover modules using a custom key, useful for application
	 * frameworks.
	 * 
	 * @param manifestKey
	 * 		manifest key to look in
	 * @return
	 */
	public Configurator discover(String manifestKey)
	{
		logger.info("Attempting discovery with key: {}", manifestKey);
		
		List<Class<Object>> modules = 
			ClassLocator.getClassModules(Configurator.class.getClassLoader(),
					Object.class, manifestKey);
		
		for(Class<Object> m : modules)
		{
			add(m);
		}
		
		return this;
	}
	
	/**
	 * Add a module to the configuration. The module will be automatically
	 * instantiated and configured.
	 * 
	 * @param type
	 * @return
	 */
	public Configurator add(Class<?> type)
	{
		if(modules.add(type))
		{
			logger.info("Adding: {}", type);
			addDependencies(type);
		}
		
		return this;
	}
	
	public Configurator addInstance(Object instance)
	{
		logger.info("Adding instance: {}", instance);
		
		moduleInstances.put(instance.getClass(), instance);
		
		modules.add(instance.getClass());
		addDependencies(instance.getClass());
		
		return this;
	}
	
	public Configurator addGuiceModule(Module module)
	{
		logger.info("Adding Guice module: {}", module);
		
		guiceModules.add(module);
		addInstance(module);
		
		return this;
	}
	
	public Configurator addGuiceModule(Class<? extends Module> type)
	{
		logger.info("Adding Guice module: {}", type);
		
		guiceModules.add(
			configurationInjector.getInstance(type)
		);
		add(type);
		
		return this;
	}
	
	private Object getInstance(Class<?> type)
	{
		Object o = moduleInstances.get(type);
		if(o == null)
		{
			o = configurationInjector.getInstance(type);
			moduleInstances.put(type, o);
		}
		
		return o;
	}
	
	/**
	 * Configure and start services.
	 */
	public void configure()
	{
		logger.info("Performing configuration and startup");
		
		// init descriptors
		initModuleDescriptors();
		
		// go through each and check if any configuration should be done
		performContributions();
		
		// retrieve ServiceManager and start all managed services
		ServiceManager manager = injector.getInstance(ServiceManager.class);
		manager.startAll();
	}
	
	private void addDependencies(Class<?> type)
	{
		Dependencies deps = type.getAnnotation(Dependencies.class);
		if(deps != null)
		{
			for(Class<?> c : deps.value())
			{
				add(c);
			}
		}
	}
	
	/**
	 * Initialize all modules by running their module descriptors in order.
	 */
	private void initModuleDescriptors()
	{
		logger.debug("Initializing service descriptions");
		
		MethodResolver resolver = new MethodResolver(Description.class,
			new MethodResolverCallback()
			{
				public Object getInstance(Class<?> c)
				{
					return Configurator.this.getInstance(c);
				}

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
			
			}
		);
		
		for(Class<?> c : modules)
		{
			resolver.add(c);
		}
		
		final Set<MethodDef> defs = resolver.getOrder();
		resolver = null;
		
		// Use the list of basic Guice modules and add the custom module
		guiceModules.add(new Module()
		{
			public void configure(Binder binder)
			{
				Object[] params = new Object[] { binder };
				
				for(MethodDef def : defs)
				{
					Method method = def.getMethod();
					Object object = def.getObject();
					
					try
					{
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
		});
		injector = Guice.createInjector(guiceModules);
	}
	
	/**
	 * Run all contributions on all modules.
	 */
	private void performContributions()
	{
		logger.debug("Performing contributions");
		
		MethodResolver resolver = new MethodResolver(Contribution.class,
			new MethodResolverCallback()
			{
				public Object getInstance(Class<?> c)
				{
					return Configurator.this.getInstance(c);
				}

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
			
			}
		);
		
		for(Class<?> c : modules)
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
	
	/**
	 * Get the injector that has been created.
	 * 
	 * @return
	 */
	public Injector getInjector()
	{
		return injector;
	}
	
}