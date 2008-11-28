package se.l4.crayon.osgi.internal;

import org.osgi.framework.ServiceReference;

import se.l4.crayon.osgi.ServiceEvent;

public class ServiceEventImpl
	implements ServiceEvent
{
	private final Type type;
	private final ServiceReference ref;
	private final boolean preferredChanged;
	
	public ServiceEventImpl(Type type, ServiceReference ref, boolean preferredChanged)
	{
		this.type = type;
		this.ref = ref;
		this.preferredChanged = preferredChanged;
	}

	public ServiceReference getOsgiRef()
	{
		return ref;
	}

	public Type getType()
	{
		return type;
	}

	public boolean preferredChanged()
	{
		return preferredChanged;
	}

}
