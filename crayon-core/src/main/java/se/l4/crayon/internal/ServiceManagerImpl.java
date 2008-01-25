package se.l4.crayon.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger =
		LoggerFactory.getLogger(ServiceManagerImpl.class);
	
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
		logger.info("Starting service: {}", service);
		
		service.start();
	}

	public void stopService(ManagedService service)
		throws Exception
	{
		logger.info("Stopping service: {}", service);
		
		service.stop();
	}

	public void startAll()
	{		
		for(ManagedService service : services)
		{
			try
			{
				startService(service);
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
				stopService(service);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				// FIXME: HANDLE EXCEPTION
			}
		}
	}
}
