/*
 * Copyright 2008 Andreas Holstenson
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.l4.crayon.services;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Test;

import com.google.inject.Injector;

import se.l4.crayon.services.internal.ServiceManagerImpl;

/**
 * Testing of {@link ServiceManagerImpl}.
 *
 * @author Andreas Holstenson
 *
 */
public class ServiceManagerTest
{
	@Test
	public void testAddAndStartAll()
		throws Exception
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

		@Override
		public void start()
		{
		}

		@Override
		public void stop()
		{
		}
	}
}
