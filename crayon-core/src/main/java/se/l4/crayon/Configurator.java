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
package se.l4.crayon;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.internal.InternalConfiguratorModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Entry point for system, used for defining which modules should be used and
 * what services that should be started. Modules need to implement
 * {@link Module}, or extend {@link AbstractModule}. See 
 * <a href="http://code.google.com/p/google-guice/">Guice website</a> for more
 * information about modules.
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
	/** Logger used within the configurator. */
	private Logger logger;
	
	/** The stage the built injector will be in. */
	private final Stage stage;

	/** List containing all the Guice modules. */
	private final List<Object> modules;
	
	/** Internal module used by configurator. */
	private final InternalConfiguratorModule internalModule;
	/** Injector created via {@link #configure()}. */
	private Injector injector;

	/** Parent injector. */
	private Injector parentInjector;

	private boolean autoStart;
	
	/**
	 * Create a configurator using {@link Stage#PRODUCTION}.
	 * 
	 */
	public Configurator()
	{
		this(Stage.PRODUCTION);
	}
	
	/**
	 * Create a new configurator in the specified stage.
	 * 
	 * @param stage
	 */
	public Configurator(Stage stage)
	{
		this.stage = stage;
		
		modules = new ArrayList<Object>();
		
		// add default module
		internalModule = new InternalConfiguratorModule(this);
		modules.add(internalModule);
		
		logger = LoggerFactory.getLogger(Configurator.class);
		
		// run start() by default in configure
		autoStart = true;
	}
	
	/**
	 * Set the logger to use, useful for wrapping configurators.
	 * 
	 * @param logger
	 */
	public Configurator setLogger(Logger logger)
	{
		this.logger = logger;
		
		return this;
	}

	/**
	 * Set which parent injector to use. Using a parent injector will
	 * cause {@link Injector#createChildInjector(Module...)} to be used
	 * instead of {@link Guice#createInjector(Module...)}.
	 * 
	 * @param injector
	 * @return
	 */
	public Configurator setParentInjector(Injector injector)
	{
		this.parentInjector = injector;

		return this;
	}

	/**
	 * Get the current parent injector.
	 * 
	 * @return
	 */
	public Injector getParentInjector()
	{
		return parentInjector;
	}
	
	/**
	 * Set if the configurator should automatically call {@link Crayon#start()}
	 * when {@link #configure()} is run.
	 * 
	 * @param autoStart
	 */
	public Configurator setAutoStart(boolean autoStart)
	{
		this.autoStart = autoStart;
		
		return this;
	}
	
	/**
	 * Add a module to the configurator.
	 * 
	 * @param instance
	 * @return
	 */
	public Configurator add(Module instance)
	{
		if(modules.add(instance))
		{
			logger.info("Adding: {}", instance);
		}
		
		return this;
	}
	
	/**
	 * Add a module to this configurator.
	 * 
	 * @param module
	 * @return
	 */
	public Configurator add(Class<? extends Module> module)
	{
		if(modules.add(module))
		{
			logger.info("Adding: {}", module);
		}
		
		return this;
	}
	
	/**
	 * Configure and start services.
	 */
	@SuppressWarnings("rawtypes")
	public Injector configure()
	{
		logger.info("Performing configuration and startup");
		
		List<Module> modules = new ArrayList<Module>();
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
				throw new ConfigurationException("Got an instance of " + m.getClass() + ", but it does not seem to be a Guice module");
			}
		}
		
		injector = parentInjector == null
			? Guice.createInjector(stage, modules)
			: parentInjector.createChildInjector(modules);
			
		if(autoStart)
		{
			injector
				.getInstance(Crayon.class)
				.start();
		}
		
		return injector;
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
	 * Get the stage of the configurator.
	 * 
	 * @return
	 */
	public Stage getStage()
	{
		return stage;
	}
}
