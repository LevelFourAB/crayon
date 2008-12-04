package se.l4.crayon.osgi.internal;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.ServiceEvent;
import se.l4.crayon.osgi.ServiceListener;
import se.l4.crayon.osgi.ServiceRef;

/**
 * Implementation of {@link ServiceRef}.
 * 
 * @author Andreas Holstenson
 *
 * @param <T>
 */
public class ServiceRefImpl<T>
	implements ServiceRef<T>
{
	private final BundleContext ctx;
	private final CopyOnWriteArrayList<ServiceListener<T>> listeners;
	
	private final Filter filter;
	private final TreeMap<ServiceReference, T> refs;
	private final Class<T> type;
	
	public ServiceRefImpl(BundleContext ctx, Filter filter, Class<T> type)
	{
		this.type = type;
		this.ctx = ctx;
		this.filter = filter;
		
		listeners = new CopyOnWriteArrayList<ServiceListener<T>>();
		refs = new TreeMap<ServiceReference, T>();
	}

	public void addServiceListener(ServiceListener<T> listener)
	{
		if(listeners.addIfAbsent(listener))
		{
			if(refs.size() != 0)
			{
				listener.serviceAvailable(this);
			}
		}
	}
	
	public void removeServiceListener(ServiceListener<T> listener)
	{
		listeners.remove(listener);
	}

	public T get()
	{
		// The last ServiceReference has priority
		synchronized(refs)
		{
			return refs.isEmpty()
				? null
				: refs.get(refs.lastKey());
		}
	}
	
	@SuppressWarnings("unchecked")
	public T get(ServiceReference ref)
	{
		return (T) ctx.getService(ref);
	}
	
	public Iterable<T> getAll()
	{
		return new Iterable<T>()
		{
			public Iterator<T> iterator()
			{
				return new Iterator<T>()
				{
					private Object[] services = 
						refs.values().toArray();
					
					private int index = services.length-1;
					
					public boolean hasNext()
					{
						return index >= 0;
					}
					
					@SuppressWarnings("unchecked")
					public T next()
					{
						return (T) services[index--];
					}
					
					public void remove()
					{
					}
				};
			}
		};
	}

	public ServiceReference[] getReferences()
	{
		synchronized(refs)
		{
			return refs.keySet()
				.toArray(new ServiceReference[refs.size()]);
		}
	}
	
	public Filter getFilter()
	{
		return filter;
	}
	
	public boolean isAvailable()
	{
		return false == refs.isEmpty();
	}
	
	public void shutdown()
	{
		synchronized(refs)
		{
			for(ServiceReference ref : refs.keySet())
			{
				ctx.ungetService(ref);
			}
		}
	}
	
	public void addServiceReference(ServiceReference ref)
	{
		boolean highestModified;
		boolean empty;
		
		if(false == ref.isAssignableTo(ctx.getBundle(), type.getName()))
		{
			return;
		}
		
		@SuppressWarnings("unchecked")
		T object = (T) ctx.getService(ref);
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.lastKey();
			refs.put(ref, object);
			
			highestModified = highest != refs.lastKey();
		}
		
		if(empty)
		{
			for(ServiceListener<T> l : listeners)
			{
				l.serviceAvailable(this);
			}
		}

		for(ServiceListener<T> l : listeners)
		{
			l.serviceModified(this, 
				new ServiceEventImpl(ServiceEvent.Type.ADDED, ref, highestModified)
			);
		}
	}
	
	public void removeServiceReference(ServiceReference ref)
	{
		boolean highestModified;
		boolean empty;
		boolean removed;
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.lastKey();
			removed = refs.remove(ref) != null;
			
			empty = refs.isEmpty();
			highestModified = highest != (empty ? null : refs.lastKey());
		}
		
		if(empty)
		{
			for(ServiceListener<T> l : listeners)
			{
				l.serviceUnavailable(this);
			}
		}

		for(ServiceListener<T> l : listeners)
		{
			l.serviceModified(this, 
				new ServiceEventImpl(ServiceEvent.Type.REMOVED, ref, highestModified)
			);
		}
		
		if(removed)
		{
			ctx.ungetService(ref);
		}
	}
	
	public void updateServiceReference(ServiceReference ref)
	{
		boolean empty;
		boolean highestModified;
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.lastKey();
			
			if(refs.remove(ref) != null)
			{
				ctx.ungetService(ref);
			}
			
			@SuppressWarnings("unchecked")
			T object = (T) ctx.getService(ref);
			refs.put(ref, object);
			
			highestModified = highest != (empty ? null : refs.lastKey());
		}
		
		for(ServiceListener<T> l : listeners)
		{
			l.serviceModified(this, 
				new ServiceEventImpl(ServiceEvent.Type.MODIFIED, ref, highestModified)
			);
		}
	}
}
