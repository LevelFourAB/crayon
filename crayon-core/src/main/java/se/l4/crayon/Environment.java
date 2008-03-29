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
 * Environment that the system is running in. Can be given to 
 * {@link Configurator} at startup to define the environment of the system.
 * 
 * @author Andreas Holstenson
 *
 */
public enum Environment
{
	/**
	 * Development environment, performs more runtime checks and outputs
	 * information that is useful during development.
	 */
	DEVELOPMENT,
	
	/**
	 * Production environment, the system is optimized for performance and
	 * performs less runtime checks.
	 */
	PRODUCTION
}
