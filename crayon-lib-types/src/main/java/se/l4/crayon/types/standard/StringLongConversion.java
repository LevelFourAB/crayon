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
package se.l4.crayon.types.standard;

import se.l4.crayon.types.ConversionException;
import se.l4.crayon.types.Conversion;

public class StringLongConversion
	implements Conversion<String, Long>
{

	public Long convert(String in)
	{
		if(in == null)
		{
			return 0l;
		}
		
		try
		{
			return Long.parseLong(in);
		}
		catch(NumberFormatException e)
		{
			throw new ConversionException("Can not convert input to long; "
				+ e.getMessage(), e); 
		}
	}

	public Class<String> getInput()
	{
		return String.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
