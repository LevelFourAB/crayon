package se.l4.crayon.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.crayon.ManagedService;
import se.l4.crayon.ServiceManager;

/**
 * Implementation of {@link ServiceManager}.  
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ServiceManagerImpl
	implements ServiceManager
{
	private Injector injector;
	private Set<ManagedService> services;
	
	@Inject
	public ServiceManagerImpl(Injector injector)
	{
		services = new CopyOnWriteArraySet<ManagedService>();
		
		this.injector = injector;
	}
	
	public void addService(ManagedService service)
	{
		services.add(service);
	}
	
	public ManagedService addService(Class<? extends ManagedService> service)
	{
		ManagedService instance = injector.getInstance(service);
		addService(instance);
		
		return instance;
	}
	
	public void startService(ManagedService service)
		throws Exception
	{
		service.start();
	}

	public void stopService(ManagedService service)
		throws Exception
	{
		service.stop();
	}

	public void startAll()
	{		
		for(ManagedService service : services)
		{
			try
			{
				service.start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				// FIXME: HANDLE EXCEPTION
			}
		}
	}

	public void stopAll()
	{
		for(ManagedService service : services)
		{
			try
			{
				service.stop();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				// FIXME: HANDLE EXCEPTION
			}
		}
	}
}
