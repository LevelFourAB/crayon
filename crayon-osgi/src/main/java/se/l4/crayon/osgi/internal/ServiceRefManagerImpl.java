package se.l4.crayon.osgi.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
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
	private final Map<String, List<ServiceRefImpl<?>>> refs;
	
	@Inject
	public ServiceRefManagerImpl(BundleContext ctx)
	{
		this.ctx = ctx;
		
		refs = new HashMap<String, List<ServiceRefImpl<?>>>();
		
		ctx.addServiceListener(new Listener());
	}
	
	public <T> ServiceRef<T> get(Class<T> type)
	{
		return get(type, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> ServiceRef<T> get(Class<T> type, Filter filter)
	{
		String name = type.getName();
		
		ServiceRefImpl<?> ref = null;
		boolean newRef = false;
		
		synchronized(refs)
		{
			List<ServiceRefImpl<?>> filtered = refs.get(name);
			if(filtered == null)
			{
				filtered = new LinkedList<ServiceRefImpl<?>>();
				refs.put(name, filtered);
			}
			else
			{
				for(ServiceRefImpl<?> r : filtered)
				{
					Filter rf = r.getFilter();
					if(rf == null && filter == null)
					{
						ref = r;
						break;
					}
					else if(rf != null && rf.equals(filter))
					{
						ref = r;
						break;
					}
				}
			}
			
			if(ref == null)
			{
				ref = new ServiceRefImpl<T>(ctx, filter, type);
				newRef = true;
				filtered.add(ref);
			}
		}
		
		if(newRef)
		{
			try
			{
				ServiceReference[] sr = ctx.getServiceReferences(name, null);
				if(sr != null)
				{
					for(ServiceReference r : sr)
					{
						ref.addServiceReference(r);
					}
				}
			}
			catch(InvalidSyntaxException e)
			{
			}
		}
		
		return (ServiceRef<T>) ref;
	}
	
	public <T> void addServiceListener(Class<T> type, se.l4.crayon.osgi.ServiceListener<T> listener) 
	{
		ServiceRef<T> ref = get(type);
		ref.addServiceListener(listener);
	}
	
	@Override
	public <T> void addServiceListener(Class<T> type, Filter filter,
			se.l4.crayon.osgi.ServiceListener<T> listener) 
	{
		ServiceRef<T> ref = get(type, filter);
		ref.addServiceListener(listener);
	}
	
	public void shutdown()
	{
		synchronized(refs)
		{
			for(List<ServiceRefImpl<?>> list : refs.values())
			{
				for(ServiceRefImpl<?> ref : list)
				{
					ref.shutdown();
				}
			}
		}
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
							List<ServiceRefImpl<?>> list = refs.get(s);
							if(list != null)
							{
								for(ServiceRefImpl<?> ref : list)
								{
									Filter rf = ref.getFilter();
									if(rf == null || rf.match(sr))
									{
										ref.addServiceReference(sr);
									}
								}
							}
						}
					}
					break;
				case ServiceEvent.UNREGISTERING:
					for(String s : types)
					{
						synchronized(refs)
						{
							List<ServiceRefImpl<?>> list = refs.get(s);
							if(list != null)
							{
								for(ServiceRefImpl<?> ref : list)
								{
									ref.removeServiceReference(sr);
								}
							}
						}
					}
					break;
			}
		}
		
	}
}
