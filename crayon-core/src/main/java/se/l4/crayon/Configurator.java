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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.annotation.Shutdown;
import se.l4.crayon.internal.InternalConfiguratorModule;
import se.l4.crayon.internal.WrapperModule;

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
 * Crayon uses <a href="http://slf4j.org/">slf4j</a> for logging. Slf4j should
 * be configured before the {@code Configurator} is used.
 * 
 * 
 * @author Andreas Holstenson
 *
 */
public class Configurator
{
	/** Manifest key used for default module discovery. */
	public static final String MANIFEST_KEY = "System-Modules";
	
	/** Logger used within the configurator. */
	private Logger logger;
	
	/** Environment that the configurator is working in. */
	private Environment environment;

	/** List containing all the Guice modules. */
	private List<Object> modules;
	
	/** Internal module used by configurator. */
	private InternalConfiguratorModule internalModule;
	/** Injector created via {@link #configure()}. */
	private Injector injector;

	/** Parent injector. */
	private Injector parentInjector;
	
	/**
	 * Create a configurator using {@link Environment#DEVELOPMENT}.
	 * 
	 */
	public Configurator()
	{
		this(Environment.DEVELOPMENT);
	}
	
	/**
	 * Create a configurator in the given environment.
	 * 
	 * @param environment
	 */
	public Configurator(Environment environment)
	{
		this.environment = environment;
		
		modules = new LinkedList<Object>();
		
		// add default module
		internalModule = new InternalConfiguratorModule(this);
		modules.add(internalModule);
		
		logger = LoggerFactory.getLogger(Configurator.class);
	}
	
	/**
	 * Set the logger to use, useful for wrapping configurators.
	 * 
	 * @param logger
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public Configurator setParentInjector(Injector injector)
	{
		this.parentInjector = injector;

		return this;
	}

	public Injector getParentInjector()
	{
		return parentInjector;
	}
	
	/**
	 * Add a module to the configurator.
	 * 
	 * @param instance
	 * @return
	 */
	public Configurator add(Object instance)
	{
		if(modules.add(instance))
		{
			logger.info("Adding: {}", instance);
			
			if(environment == Environment.DEVELOPMENT)
			{
				checkModule(instance instanceof Class
					? (Class) instance : instance.getClass());
			}
		}
		
		return this;
	}
	
	/**
	 * Add a module instance.
	 * 
	 * @param instance
	 * 		instance to add
	 * @return
	 * 		self
	 * @deprecated
	 * 		use @{link {@link #add(Object)}} instead
	 */
	@Deprecated
	public Configurator addInstance(Object instance)
	{
		add(instance);
		
		return this;
	}
	
	/**
	 * Add a Guice module to the configurator, see
	 * {@link #addGuiceModule(Class)}.
	 * 
	 * @param module
	 * 		module instance
	 * @return
	 * 		self
	 * @deprecated
	 * 		use {@link #add(Object)} instead
	 */
	@Deprecated
	public Configurator addGuiceModule(Module module)
	{
		logger.info("Adding: {}", module);
		
		modules.add(module);
		
		return this;
	}
	
	/**
	 * Add a Guice module to the configurator, used for backwards compatibility
	 * with projects that only utilize Guice. Such modules will not have the
	 * added benefits of description ordering.
	 * 
	 * @param type
	 * 		base class for Guice module
	 * @return
	 * 		self
	 * @deprecated
	 * 		use {@link #add(Object)} instead
	 */
	@Deprecated
	public Configurator addGuiceModule(Class<? extends Module> type)
	{
		add(type);
		
		return this;
	}
	
	/**
	 * Configure and start services.
	 */
	public void configure()
	{
		logger.info("Performing configuration and startup");
		
		List<Module> modules = new LinkedList<Module>();
		for(Object m : this.modules)
		{
			if(m instanceof Class)
			{
				try
				{
					m = ((Class) m).newInstance();
				}
				catch(InstantiationException e)
				{
					throw new ConfigurationException("Unable to create module, a public no-args constructor required; " + e.getMessage(), e);
				}
				catch(IllegalAccessException e)
				{
					throw new ConfigurationException("Unable to create module, a public no-args constructor required; " + e.getMessage(), e);
				}
			}
			
			if(m instanceof Module)
			{
				modules.add((Module) m);
			}
			else
			{
				modules.add(new WrapperModule(m));
			}
		}
		
		Stage stage = getStageFor(environment);
		injector = parentInjector == null
			? Guice.createInjector(stage, modules)
			: parentInjector.createChildInjector(modules);
	}
	
	/**
	 * Translate environment to Guice {@link Stage}.
	 * 
	 * @param environment
	 * 		environment to translate
	 * @return
	 * 		suitable Guice stage
	 */
	private Stage getStageFor(Environment environment)
	{
		return environment == Environment.PRODUCTION
			? Stage.PRODUCTION
			: Stage.DEVELOPMENT; 
	}
	
	/**
	 * Check a module for public methods that are not annotated with either
	 * {@link Description} or {@link Contribution} and not inherited from 
	 * {@link Object}. Will log a warning if such methods are found.
	 * 
	 * @param module
	 * 		module to check
	 */
	private void checkModule(Class<?> module)
	{
		for(Method m  : module.getMethods())
		{
			if(m.isAnnotationPresent(Description.class)
				|| m.isAnnotationPresent(Contribution.class)
				|| m.isAnnotationPresent(Shutdown.class))
			{
				continue;
			}
			
			try
			{
				Object.class.getMethod(m.getName(), m.getParameterTypes());
			}
			catch(NoSuchMethodException e)
			{
				try
				{
					Module.class.getMethod(m.getName(), m.getParameterTypes());
				}
				catch(NoSuchMethodException e2)
				{
					logger.warn("Found public non-annotated method in {}", module);
					break;
				}
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
	
	/**
	 * Perform shutdown of everything created by this configurator. Will call
	 * methods annotated with {@link Shutdown}.
	 */
	public void shutdown()
	{
		injector.getInstance(Crayon.class)
			.shutdown();
	}
	
	/**
	 * Get the environment of the configurator.
	 * 
	 * @return
	 * 		environment
	 */
	public Environment getEnvironment()
	{
		return environment;
	}
}
