/*
 * Copyright 2011 Level Four AB
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
 * Status of a service.
 * 
 * @author Andreas Holstenson
 *
 */
public enum ServiceStatus
{
	/** 
	 * Status is unknown (service has not passed through 
	 * {@link ServiceManager}).
	 */
	UNKNOWN,
	/** Service is stopping. */
	STOPPING,
	/** Service is stopped. */
	STOPPED,
	/** Service is starting. */
	STARTING,
	/** Service is running. */
	RUNNING,
	/** Service failed to start. */
	FAILED
}