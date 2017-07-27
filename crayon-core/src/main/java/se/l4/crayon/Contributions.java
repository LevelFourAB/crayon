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
package se.l4.crayon;

import com.google.inject.Module;

/**
 * Set of contributions. The contributions are bound via
 * {@link CrayonModule#bindContributions(Class)} or
 * {@link CrayonBinder#bindContributions(Class)}.
 *
 * @author Andreas Holstenson
 *
 */
public interface Contributions
{
	/**
	 * Run all contributions.
	 *
	 */
	void run();

	/**
	 * Run all contributions but activate the given modules first.
	 *
	 * @param modules
	 */
	void run(Module... modules);
}
