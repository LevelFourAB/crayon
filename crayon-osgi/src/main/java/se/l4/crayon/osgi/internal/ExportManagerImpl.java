package se.l4.crayon.osgi.internal;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import se.l4.crayon.osgi.Export;
import se.l4.crayon.osgi.ExportManager;

@Singleton
public class ExportManagerImpl
	implements ExportManager
{
	private BundleContext ctx;
	private Injector injector; 
	
	@Inject
	public ExportManagerImpl(BundleContext ctx, Injector injector)
	{
		this.ctx = ctx;
		this.injector = injector;
	}
	
	public <T> ServiceRegistration export(Class<T> type)
	{
		return export(type, (Map<String, Object>) null);
	}
	
	public <T> ServiceRegistration export(Class<T> type, String... attributes)
	{
		if(attributes.length % 2 != 0)
		{
			throw new IllegalArgumentException("attributes must be given in pairs of key and value");
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		for(int i=0, n=attributes.length; i<n; i+=2)
		{
			result.put(attributes[i], attributes[i+1]);
		}
		
		return export(type, result);
	}
	
	public <T> ServiceRegistration export(Class<T> type, Map<String, Object> attributes) 
	{
		Hashtable<String, Object> t = new Hashtable<String, Object>();
		if(attributes != null)
		{
			t.putAll(attributes);
		}
		
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
		
		return ctx.registerService(type.getName(), factory, t);
	}
	
	public <T> ServiceRegistration export(T object, Map<String, Object> attributes,
			Class<?>... types) 
	{
		Hashtable<String, Object> t = new Hashtable<String, Object>();
		if(attributes != null)
		{
			t.putAll(attributes);
		}
		
		Class<?> objClass = object.getClass();
		String[] classes = new String[types.length];
		for(int i=0, n=classes.length; i<n; i++)
		{
			Class<?> c = types[i];
			if(false == c.isAssignableFrom(objClass))
			{
				throw new IllegalArgumentException("Unable to export as " + c + ", it is not compatible with " + objClass);
			}
			
			classes[i] = c.getName();
		}
		
		return ctx.registerService(classes, object, t);
	}
	
	public <T> void remove(ServiceRegistration registration)
	{
		if(registration != null)
		{
			registration.unregister();
		}
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
