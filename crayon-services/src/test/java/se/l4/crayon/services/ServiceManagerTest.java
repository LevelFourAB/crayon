package se.l4.crayon.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import se.l4.crayon.services.internal.ServiceManagerImpl;

/**
 * Testing of {@link ServiceManagerImpl}.
 *
 */
public class ServiceManagerTest
{
	@Test
	public void testStartAllSingle()
		throws Exception
	{
		// create manager and add service and request start
		ServiceManager manager = new ServiceManagerImpl();
		manager.add(new ServiceA());

		manager.startAll();

		assertThat(manager.get(ServiceA.class).get().getStatus(), is(ServiceStatus.RUNNING));
	}

	@Test
	public void testStartAndStopWithDependency()
		throws Exception
	{
		// create manager and add service and request start
		ServiceManager manager = new ServiceManagerImpl();
		manager.add(new ServiceA());
		manager.add(new ServiceB());

		manager.start(ServiceB.class);

		assertThat(manager.get(ServiceA.class).get().getStatus(), is(ServiceStatus.RUNNING));
		assertThat(manager.get(ServiceB.class).get().getStatus(), is(ServiceStatus.RUNNING));

		manager.stop(ServiceA.class);

		assertThat(manager.get(ServiceA.class).get().getStatus(), is(ServiceStatus.STOPPED));
		assertThat(manager.get(ServiceB.class).get().getStatus(), is(ServiceStatus.STOPPED));
	}

	private static class ServiceA
		implements ManagedService
	{

		@Override
		public void start(ServiceEncounter encounter)
		{
		}

		@Override
		public void stop()
		{
		}
	}

	private static class ServiceB
		implements ManagedService
	{

		@Override
		public void start(ServiceEncounter encounter)
		{
		}

		@Override
		public void stop()
		{
		}

		@Override
		public Set<Class<? extends ManagedService>> getDependencies()
		{
			return Collections.singleton(ServiceA.class);
		}
	}
}
