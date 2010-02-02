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
 * Generic type conversion, used for converting any type to any other type.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TypeConverter
{
	/**
	 * Convert the given input to another type.
	 * 
	 * @param <T>
	 * 		type of output
	 * @param in
	 * 		value to convert (input)
	 * @param output
	 * 		output type
	 * @return
	 * 		converted value
	 * @throws ConversionException
	 * 		if unable to convert
	 */
	<T> T convert(Object in, Class<T> output);

	/**
	 * Add a conversion between two types.
	 * 
	 * @param conversion
	 * 		conversion
	 */
	void add(Conversion<?, ?> conversion);
	
	/**
	 * Check if a conversion is supported.
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	boolean canConvertBetween(Class<?> in, Class<?> out);
}
