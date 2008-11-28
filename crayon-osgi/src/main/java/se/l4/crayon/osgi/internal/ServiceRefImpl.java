package se.l4.crayon.osgi.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
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
//	private static final Comparator<ServiceReference> COMPARATOR = 
//		new Comparator<ServiceReference>()
//		{
//			public int compare(ServiceReference o1, ServiceReference o2)
//			{
//				return o1.
//			};
//		};
		
	private final BundleContext ctx;
	private final CopyOnWriteArrayList<ServiceListener<T>> listeners;
	private final AtomicInteger gets;
	
//	private ServiceReference ref;
	private final TreeSet<ServiceReference> refs;
	
	public ServiceRefImpl(BundleContext ctx)
	{
		this.ctx = ctx;
		
		listeners = new CopyOnWriteArrayList<ServiceListener<T>>();
		refs = new TreeSet<ServiceReference>();
		gets = new AtomicInteger();
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

	@SuppressWarnings("unchecked")
	public T get()
	{
		// Try to get a service reference
		ServiceReference ref;
		
		synchronized(refs)
		{
			ref = refs.last();
		}
		
		if(ref != null)
		{
			gets.incrementAndGet();
			return (T) ctx.getService(ref);
		}
		
		return null;
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
					private Object[] refs = 
						ServiceRefImpl.this.refs.toArray();
					
					private int index = refs.length-1;
					
					public boolean hasNext()
					{
						return index >= 0;
					}
					
					@SuppressWarnings("unchecked")
					public T next()
					{
						ServiceReference ref = (ServiceReference) refs[index--];
						
						return (T) ctx.getService(ref);
					}
					
					public void remove()
					{
					}
				};
			}
		};
	}

	@Override
	public ServiceReference[] getReferences()
	{
		synchronized(refs)
		{
			return refs.toArray(new ServiceReference[refs.size()]);
		}
	}
	
	public boolean isAvailable()
	{
		return false == refs.isEmpty();
	}
	
	public void unget()
	{
//		if(ref != null && gets.decrementAndGet() <= 0)
//		{
//			ctx.ungetService(ref);
//		}
	}
	
	public void addServiceReference(ServiceReference ref)
	{
		boolean highestModified;
		boolean empty;
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.last();
			refs.add(ref);
			
			highestModified = highest != refs.last();
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
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.last();
			refs.remove(ref);
			
			empty = refs.isEmpty();
			highestModified = highest != (empty ? null : refs.last());
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
		
//		ctx.ungetService(ref);
	}
	
	public void updateServiceReference(ServiceReference ref)
	{
		boolean empty;
		boolean highestModified;
		
		synchronized(refs)
		{
			empty = refs.isEmpty();
			
			ServiceReference highest = empty ? null : refs.last();
			
			refs.remove(ref);
			refs.add(ref);
			
			highestModified = highest != (empty ? null : refs.last());
		}
		
		for(ServiceListener<T> l : listeners)
		{
			l.serviceModified(this, 
				new ServiceEventImpl(ServiceEvent.Type.MODIFIED, ref, highestModified)
			);
		}
	}
}
