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

public class ConversionTestHelper
{
	private ConversionTestHelper()
	{
		
	}
	
	public static void convert(TypeConverter tc, Object value, Class<?> type, 
			Object expected)
	{
		Object result = tc.convert(value, type);
		
		if(expected == null && result != null)
		{
			throw new AssertionError("Invalid value returned, got: " + result);
		}
		
		if(expected != null && result != null 
			&& false == expected.equals(result))
		{
			throw new AssertionError("Invalid value returned, got: " + result);
		}
	}
	
	public static void convertFail(TypeConverter tc, Object value, Class<?> type)
	{
		try
		{
			Object result = tc.convert(value, type);
			throw new AssertionError("Conversion dit not fail, got: " + result);
		}
		catch(ConversionException e)
		{
		}
	}
}
