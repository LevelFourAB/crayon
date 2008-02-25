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
package se.l4.crayon;

/**
 * Manager of services, used for starting and stopping all system services.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServiceManager
{
	/**
	 * Add a service that should be managed.
	 * 
	 * @param service
	 */
	void addService(ManagedService service);
	
	/**
	 * Add a service that should be managed. Will resolve the class and
	 * create an instance of the service to be used.
	 * 
	 * @param service
	 * @return
	 * 		instance of service that was added
	 */
	ManagedService addService(Class<? extends ManagedService> service);
	
	/**
	 * Start the given service. Will check if it has already been started
	 * and refuse to start if it has.
	 * 
	 * @param service
	 * @throws Exception 
	 */
	void startService(ManagedService service) throws Exception;

	/**
	 * Stop the given service. Stops the service if it is running.
	 * 
	 * @param service
	 * @throws Exception 
	 */
	void stopService(ManagedService service) throws Exception;
	
	/**
	 * Start all services.
	 */
	void startAll();
	
	/**
	 * Stop all services.
	 */
	void stopAll();
}
