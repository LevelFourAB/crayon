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
 * Compound conversion used for chaining two type conversions together.
 *  
 * @author Andreas Holstenson
 *
 */
public class CompoundTypeConversion
	implements Conversion<Object, Object>
{
	private final Conversion<Object, Object> in;
	private final Conversion<Object, Object> out;
	
	public CompoundTypeConversion(Conversion<Object, Object> in, 
			Conversion<Object, Object> out)
	{
		this.in = in;
		this.out = out;
	}
	
	public Object convert(Object in)
	{
		Object firstPass = this.in.convert(in);
		return out.convert(firstPass);
	}

	public Class<Object> getInput()
	{
		return in.getInput();
	}

	public Class<Object> getOutput()
	{
		return out.getOutput();
	}

}
