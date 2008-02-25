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
 * Managed service, service that is started/stopped by the system kernel.
 * Should be marked as a singleton (via {@link com.google.inject.Singleton}
 * annotation).
 * 
 * @author Andreas Holstenson
 *
 */
public interface ManagedService
{
	/** Start service. */
	void start() throws Exception;
	
	/** Stop service. */
	void stop() throws Exception;
}
