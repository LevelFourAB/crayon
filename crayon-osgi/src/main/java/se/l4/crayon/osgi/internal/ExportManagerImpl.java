package se.l4.crayon.osgi.internal;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import se.l4.crayon.osgi.Export;
import se.l4.crayon.osgi.ExportManager;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ExportManagerImpl
	implements ExportManager
{
	private BundleContext ctx;
	private Injector injector;
	
	private Map<Class<?>, ServiceRegistration> registrations;
	
	@Inject
	private ExportManagerImpl(BundleContext ctx, Injector injector)
	{
		this.ctx = ctx;
		this.injector = injector;
		
		registrations = new ConcurrentHashMap<Class<?>, ServiceRegistration>();
	}
	
	public <T> void export(Class<T> type)
	{
		Hashtable<String, Object> t = new Hashtable<String, Object>();
		
		final Provider<T> provider = injector.getProvider(type);
		ServiceFactory factory = new ServiceFactory()
		{

			public Object getService(Bundle arg0, ServiceRegistration arg1)
			{
				return provider.get();
			}

			public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2)
			{
				// TODO: What should happen on unget?
			}
			
		};
		
		ServiceRegistration reg = ctx.registerService(type.getName(), factory, t);
		registrations.put(type, reg);
	}
	
	public <T> void remove(Class<T> type)
	{
		ServiceRegistration reg = registrations.remove(type);
		if(reg != null)
		{
			reg.unregister();
		}
	}

	public void removeAll()
	{
		for(ServiceRegistration reg : registrations.values())
		{
			reg.unregister();
		}
		
		registrations.clear();
	}

	/**
	 * Automatically export anything that is bound and annotated with
	 * {@link Export}.
	 */
	public void autoExport()
	{
		/*
		 * Export all interfaces marked with @Export expected those that have
		 * an ExternalServiceProvider (as they are actually imported)
		 */
		String externalProvider = "ExternalServiceProvider";
		
		for(Map.Entry<Key<?>, Binding<?>> entry : injector.getBindings().entrySet())
		{
			Key<?> key = entry.getKey();
			Binding<?> binding = entry.getValue();
			
			Provider<?> provider = binding.getProvider();
			
			Type type = key.getTypeLiteral().getType();
			if(type instanceof Class<?>)
			{
				Class<?> c = (Class<?>) type;
				if(c.isAnnotationPresent(Export.class) 
					&& false == provider.toString().startsWith(externalProvider))
				{
					export(c);
				}
			}
		}
	}
}
