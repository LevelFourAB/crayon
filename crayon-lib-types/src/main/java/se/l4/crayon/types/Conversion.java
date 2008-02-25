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
package se.l4.crayon.types;

/**
 * Conversion between two types, used by the {@link TypeConverter} to perform
 * actual type conversions.
 * 
 * @author Andreas Holstenson
 *
 * @param <I>
 * 		input type
 * @param <O>
 * 		output type
 */
public interface Conversion<I, O>
{
	/**
	 * Convert the given input.
	 * 
	 * @param in
	 * 		input
	 * @return
	 * 		converted output
	 * @throws ConversionException
	 * 		if unable to convert input
	 */
	O convert(I in);
	
	/**
	 * Get class of input (should match {@code <I>} parameter).
	 * 
	 * @return
	 */
	Class<I> getInput();
	
	/**
	 * Get class of output (should match {@code <O>} parameter).
	 * 
	 * @return
	 */
	Class<O> getOutput();
}
