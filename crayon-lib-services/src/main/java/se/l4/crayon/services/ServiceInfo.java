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

/**
 * Information about a service in the system.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServiceInfo
{
	/**
	 * Get instance that the service information is for.
	 * 
	 * @return
	 * 		instance of {@link ManagedService}
	 */
	ManagedService getService();
	
	/**
	 * Get the status of the service.
	 * 
	 * @return
	 * 		service status
	 */
	ServiceStatus getStatus();
	
	/**
	 * Get the exception the service failed with, if {@link #getStatus()}
	 * returns {@link ServiceStatus#FAILED}.
	 * 
	 * @return
	 * 		exception that it failed with, or {@code null} if no failure
	 */
	Exception getFailedWith();
}
