package se.l4.crayon.osgi.internal;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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
	private final Class<T> type;
	private final CopyOnWriteArrayList<ServiceListener<T>> listeners;
	private final AtomicInteger gets;
	
	private ServiceReference ref;
	
	public ServiceRefImpl(BundleContext ctx, Class<T> type)
	{
		this.ctx = ctx;
		this.type = type;
		
		listeners = new CopyOnWriteArrayList<ServiceListener<T>>();
		gets = new AtomicInteger();
	}

	public void addServiceListener(ServiceListener<T> listener)
	{
		if(listeners.addIfAbsent(listener))
		{
			if(ref != null)
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
		if(ref == null)
		{
			ref = ctx.getServiceReference(type.getName());
		}
		
		if(ref != null)
		{
			gets.incrementAndGet();
			return (T) ctx.getService(ref);
		}
		
		return null;
	}

	public boolean isAvailable()
	{
		return ref != null;
	}
	
	public void unget()
	{
		if(ref != null && gets.decrementAndGet() <= 0)
		{
			ctx.ungetService(ref);
		}
	}
	
	public void setServiceReference(ServiceReference ref)
	{
		this.ref = ref;
		
		if(ref == null)
		{
			for(ServiceListener<T> l : listeners)
			{
				l.serviceUnavailable(this);
			}
		}
		else
		{
			for(ServiceListener<T> l : listeners)
			{
				l.serviceAvailable(this);
			}
		}
	}
}
