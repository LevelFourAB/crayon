package se.l4.crayon.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import se.l4.crayon.EntryPoint;
import se.l4.crayon.ErrorHandler;
import se.l4.crayon.ErrorManager;
import se.l4.crayon.ServiceManager;
import se.l4.crayon.annotation.ModuleDescription;

/**
 * Module that is always loaded, containing the base configuration and bindings
 * to support the system. This includes bindings to the {@link EntryPoint} and
 * {@link ServiceManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class EntryPointModule
{
	private EntryPoint entryPoint;
	private Map<String, List<Module>> discoveredModules;
	
	public EntryPointModule(EntryPoint entryPoint)
	{
		this.entryPoint = entryPoint;
		
		discoveredModules = new HashMap<String, List<Module>>();
	}
	
	public List<Module> getListForManifestKey(String manifestKey)
	{
		List<Module> modules = discoveredModules.get(manifestKey);
			
		if(modules == null)
		{
			modules = new LinkedList<Module>();
			discoveredModules.put(manifestKey, modules);
		}
		
		return modules;
	}
	
	@ModuleDescription
	public void configure(Binder binder)
	{
		// Reference to entry point
		binder.bind(EntryPoint.class).toInstance(entryPoint);
		
		// Services
		binder.bind(ServiceManager.class).to(ServiceManagerImpl.class);
		
		// Error handling
		binder.bind(ErrorManager.class).to(ErrorManagerImpl.class);
		binder.bind(ErrorHandler.class).to(ErrorHandlerImpl.class);
		
		// bind discovered modules tied to manifest keys
		for(Map.Entry<String, List<Module>> entry : discoveredModules.entrySet())
		{
			String key = entry.getKey();
			List<Module> value = Collections.unmodifiableList(entry.getValue());
			
			binder.bind(new TypeLiteral<List<Module>>(){})
				.annotatedWith(Names.named(key))
				.toInstance(value);
		}
	}
	
}
