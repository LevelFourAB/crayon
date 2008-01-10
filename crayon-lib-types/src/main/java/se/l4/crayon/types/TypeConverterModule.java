package se.l4.crayon.types;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.ModuleDescription;
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

/**
 * Module for type converter, contributes implementation and default
 * conversions.
 * 
 * @author Andreas Holstenson
 *
 */
public class TypeConverterModule
{
	@ModuleDescription
	public void bindTypeConverter(Binder binder)
	{
		binder.bind(TypeConverter.class).to(TypeConverterImpl.class);
	}
	
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
