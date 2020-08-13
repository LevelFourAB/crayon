package se.l4.crayon.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
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

		manager.startAll().blockLast();

		assertThat(manager.get(ServiceA.class).block().getState(), is(ServiceStatus.State.RUNNING));
	}

	@Test
	public void testStartAndStopWithDependency()
		throws Exception
	{
		// create manager and add service and request start
		ServiceManager manager = new ServiceManagerImpl();
		manager.add(new ServiceA());
		manager.add(new ServiceB());

		manager.start(ServiceB.class).block();

		assertThat(manager.get(ServiceA.class).block().getState(), is(ServiceStatus.State.RUNNING));
		assertThat(manager.get(ServiceB.class).block().getState(), is(ServiceStatus.State.RUNNING));

		manager.stop(ServiceA.class).block();

		assertThat(manager.get(ServiceA.class).block().getState(), is(ServiceStatus.State.STOPPED));
		assertThat(manager.get(ServiceB.class).block().getState(), is(ServiceStatus.State.STOPPED));
	}

	private static class ServiceA
		implements ManagedService
	{

		@Override
		public Mono<RunningService> start()
		{
			return Mono.just(RunningService.stoppable(() -> {}));
		}
	}

	private static class ServiceB
		implements ManagedService
	{

		@Override
		public Mono<RunningService> start()
		{
			return Mono.just(RunningService.stoppable(() -> {}));
		}

		@Override
		public ImmutableSet<Class<? extends ManagedService>> getDependencies()
		{
			return Sets.immutable.of(ServiceA.class);
		}
	}
}
