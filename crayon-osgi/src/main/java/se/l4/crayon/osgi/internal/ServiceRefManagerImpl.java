package se.l4.crayon.osgi.internal;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.ServiceRef;
import se.l4.crayon.osgi.ServiceRefManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manager of service references.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ServiceRefManagerImpl
	implements ServiceRefManager
{
	private final BundleContext ctx;
	private final Map<String, ServiceRefImpl<?>> refs;
	
	@Inject
	public ServiceRefManagerImpl(BundleContext ctx)
	{
		this.ctx = ctx;
		
		refs = new HashMap<String, ServiceRefImpl<?>>();
		
		ctx.addServiceListener(new Listener());
	}
	
	@SuppressWarnings("unchecked")
	public <T> ServiceRef<T> get(Class<T> type)
	{
		String name = type.getName();
		
		ServiceRefImpl<?> ref;
		boolean newRef = false;
		
		synchronized(refs)
		{
			ref = refs.get(name);
			
			if(ref == null)
			{
				ref = new ServiceRefImpl<T>(ctx, type);
				newRef = true;
				refs.put(name, ref);
			}
			
		}
		
		if(newRef)
		{
			ServiceReference sr = ctx.getServiceReference(name);
			if(sr != null)
			{
				ref.setServiceReference(sr);
			}
		}
		
		return (ServiceRef<T>) ref;
	}
	
	public <T> void addServiceListener(Class<T> type, se.l4.crayon.osgi.ServiceListener<T> listener) 
	{
		ServiceRef<T> ref = get(type);
		ref.addServiceListener(listener);
	}
	
	private class Listener
		implements ServiceListener
	{

		public void serviceChanged(ServiceEvent event)
		{
			ServiceReference sr = event.getServiceReference();
			Object objectClass = sr.getProperty("objectclass");
			String[] types = (String[]) objectClass;
			
			switch(event.getType())
			{
				case ServiceEvent.REGISTERED:
					for(String s : types)
					{
						synchronized(refs)
						{
							ServiceRefImpl<?> ref = refs.get(s);
							if(ref != null)
							{
								ref.setServiceReference(sr);
							}
						}
					}
					break;
				case ServiceEvent.UNREGISTERING:
					for(String s : types)
					{
						synchronized(refs)
						{
							ServiceRefImpl<?> ref = refs.get(s);
							if(ref != null)
							{
								ref.setServiceReference(null);
							}
						}
					}
					
			}
			System.out.println("===");
			
		}
		
	}
}
