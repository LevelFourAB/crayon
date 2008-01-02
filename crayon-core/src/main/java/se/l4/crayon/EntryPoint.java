package se.l4.crayon;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import se.l4.crayon.internal.ClassLocator;
import se.l4.crayon.internal.EntryPointModule;

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
 * Discovery is done via the method {@link #discoverModules()}.
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
public class EntryPoint
{
	public static final String MANIFEST_KEY = "System-Modules";
	
	private Injector configurationInjector;
	private List<Module> modules;
	
	private Injector injector;
	
	private EntryPointModule entryPointModule;
	
	public EntryPoint()
	{
		configurationInjector = Guice.createInjector();
		
		modules = new LinkedList<Module>();
		
		// add default module
		entryPointModule = new EntryPointModule(this);
		modules.add(entryPointModule);
	}
	
	/**
	 * Attempt to discover modules stored in Jar-files and add them to the
	 * configuration. Will use {@link #MANIFEST_KEY} for search.
	 * 
	 * @return
	 * 		self
	 */
	public EntryPoint discoverModules()
	{
		return discoverModules(MANIFEST_KEY);
	}
	
	/**
	 * Attempt to discover modules using a custom key, useful for application
	 * frameworks.
	 * 
	 * @param manifestKey
	 * 		manifest key to look in
	 * @return
	 */
	public EntryPoint discoverModules(String manifestKey)
	{
		List<Class<Module>> modules = 
			ClassLocator.getClassModules(EntryPoint.class.getClassLoader(),
					Module.class, manifestKey);
		
		List<Module> moduleInstances = 
			entryPointModule.getListForManifestKey(manifestKey);
		
		for(Class<Module> m : modules)
		{
			Module instance = configurationInjector.getInstance(m);
			
			this.modules.add(instance);
			moduleInstances.add(instance);
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
	public EntryPoint add(Class<? extends Module> type)
	{
		Module instance = configurationInjector.getInstance(type);
		modules.add(instance);
		
		return this;
	}
	
	/**
	 * Configure and start services as defined in the entry point.
	 */
	public void start()
	{
		injector = Guice.createInjector(modules);
		
		// configure logging (if possible)
		configureLogging();
		
		// go through each and check if any configuration should be done
		for(Module m : modules)
		{
			configure(m);
		}
		
		// retrieve ServiceManager and start all managed services
		ServiceManager manager = injector.getInstance(ServiceManager.class);
		manager.startAll();
	}
	
	private void configureLogging()
	{
		// retrieve binding first
		Binding<LoggingConfigurator> logging =
			injector.getBinding(Key.get(LoggingConfigurator.class));
		
		// if binding exists perform configuration
		if(logging != null)
		{
			LoggingConfigurator configurator = logging.getProvider().get();
			configurator.configure();
		}
	}
	
	/**
	 * Get the injector of the entry point.
	 * 
	 * @return
	 */
	public Injector getInjector()
	{
		return injector;
	}
	
	/**
	 * Run all methods named {@code configure} on the given object. Will
	 * exclude methods without parameters and the method defined by
	 * {@link Module}.
	 * 
	 * @param object
	 * 		object to run configure methods on
	 */
	private void configure(Object object)
	{
		for(Method method : object.getClass().getMethods())
		{
			if(method.isAnnotationPresent(Contribution.class))
			{
				if(false == Modifier.isPublic(method.getModifiers()))
				{
					throw new ConfigurationException("Method is not public " 
							+ method + " (" + object.getClass() + ")");
				}
				
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
	}
}
