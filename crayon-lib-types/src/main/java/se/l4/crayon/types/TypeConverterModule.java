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

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.crayon.types.standard.BooleanLongConversion;
import se.l4.crayon.types.standard.BooleanStringConversion;
import se.l4.crayon.types.standard.ByteLongConversion;
import se.l4.crayon.types.standard.DoubleFloatConversion;
import se.l4.crayon.types.standard.DoubleStringConversion;
import se.l4.crayon.types.standard.FloatDoubleConversion;
import se.l4.crayon.types.standard.IntegerLongConversion;
import se.l4.crayon.types.standard.LongBooleanConversion;
import se.l4.crayon.types.standard.LongByteConversion;
import se.l4.crayon.types.standard.LongIntegerConversion;
import se.l4.crayon.types.standard.LongShortConversion;
import se.l4.crayon.types.standard.LongStringConversion;
import se.l4.crayon.types.standard.ShortLongConversion;
import se.l4.crayon.types.standard.StringBooleanConversion;
import se.l4.crayon.types.standard.StringDoubleConversion;
import se.l4.crayon.types.standard.StringLongConversion;
import se.l4.crayon.types.standard.VoidBooleanConversion;
import se.l4.crayon.types.standard.VoidDoubleConversion;
import se.l4.crayon.types.standard.VoidLongConversion;
import se.l4.crayon.types.standard.VoidStringConversion;

import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * Module for type converter, contributes implementation and default
 * conversions.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeConverterModule
{
	/**
	 * Bind the default type converter.
	 * 
	 * @param binder
	 */
	@Description
	public void bindTypeConverter(Binder binder)
	{
		binder.bind(TypeConverter.class).to(DefaultTypeConverter.class)
			.in(Scopes.SINGLETON);
	}
	
	/**
	 * Register the default converters with {@link TypeConverter}. This
	 * includes conversion to/from strings and between numbers.
	 * 
	 * @param converter
	 */
	@Contribution
	public void contributeDefaultConversions(TypeConverter converter)
	{
		converter.add(new IntegerLongConversion());
		converter.add(new LongIntegerConversion());
		
		converter.add(new DoubleFloatConversion());
		converter.add(new FloatDoubleConversion());
		
		converter.add(new ShortLongConversion());
		converter.add(new LongShortConversion());
		
		converter.add(new ByteLongConversion());
		converter.add(new LongByteConversion());
		
		converter.add(new DoubleStringConversion());
		converter.add(new StringDoubleConversion());
		
		converter.add(new LongStringConversion());
		converter.add(new StringLongConversion());
		
		converter.add(new BooleanLongConversion());
		converter.add(new LongBooleanConversion());
		
		converter.add(new BooleanStringConversion());
		converter.add(new StringBooleanConversion());
		
		converter.add(new VoidDoubleConversion());
		converter.add(new VoidBooleanConversion());
		converter.add(new VoidLongConversion());
		converter.add(new VoidStringConversion());
	}
}
