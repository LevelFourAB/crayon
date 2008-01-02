package se.l4.crayon.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.testng.annotations.Test;

import com.google.inject.Injector;

import se.l4.crayon.ManagedService;
import se.l4.crayon.ServiceManager;
import se.l4.crayon.internal.ServiceManagerImpl;

/**
 * Testing of {@link ServiceManagerImpl}.
 * 
 * @author andreas
 *
 */
public class ServiceManagerTest
{
	@Test
	public void testAddAndStartAll()
	{
		// setup injector
		Injector injector = createMock(Injector.class);
		replay(injector);
		
		// setup managed service
		ManagedService service = createMock(ManagedService.class);
		service.start();
		replay(service);
		
		// create manager and add service and request start
		ServiceManager manager = new ServiceManagerImpl(injector);
		manager.addService(service);
		
		manager.startAll();
		
		// verify
		verify(injector, service);
	}
	
	@Test
	public void testAutoCreateCall()
	{
		// setup injector
		Injector injector = createMock(Injector.class);
		expect(injector.getInstance(DumbService.class)).andReturn(new DumbService());
		replay(injector);
		
		// create manager and add service and request start
		ServiceManager manager = new ServiceManagerImpl(injector);
		manager.addService(DumbService.class);
		
		// verify
		verify(injector);
	}
	
	private static class DumbService
		implements ManagedService
	{

		public void start()
		{
		}

		public void stop()
		{
		}
	}
}
