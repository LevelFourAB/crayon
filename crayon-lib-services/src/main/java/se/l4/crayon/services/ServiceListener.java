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

import java.util.EventListener;

/**
 * Service listener interface that can be used to monitor the status of a
 * service.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServiceListener
	extends EventListener
{
	/**
	 * Status of a service has changed.
	 * 
	 * @param info
	 * 		information about service
	 */
	void serviceStatusChanged(ServiceInfo info);
}
