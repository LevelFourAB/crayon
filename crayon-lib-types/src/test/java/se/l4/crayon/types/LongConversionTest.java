package se.l4.crayon.types;

import static se.l4.crayon.types.ConversionTestHelper.convert;
import static se.l4.crayon.types.ConversionTestHelper.convertFail;

import org.testng.annotations.Test;

import se.l4.crayon.types.standard.DoubleLongConversion;
import se.l4.crayon.types.standard.IntegerLongConversion;
import se.l4.crayon.types.standard.LongDoubleConversion;
import se.l4.crayon.types.standard.LongIntegerConversion;
import se.l4.crayon.types.standard.LongShortConversion;
import se.l4.crayon.types.standard.LongStringConversion;
import se.l4.crayon.types.standard.ShortLongConversion;
import se.l4.crayon.types.standard.StringLongConversion;

public class LongConversionTest
{
	@Test
	public void testLongToString()
	{
		TypeConverter tc = new TypeConverterImpl(null);
		tc.add(new LongStringConversion());
		tc.add(new StringLongConversion());
		
		convert(tc, "890", Long.class, 890l);
		convert(tc, 4560l, String.class, "4560");
		
		convertFail(tc, "3k4", Long.class);
	}
	
	@Test
	public void testLongToDouble()
	{
		TypeConverter tc = new TypeConverterImpl(null);
		tc.add(new LongDoubleConversion());
		tc.add(new DoubleLongConversion());
		
		convert(tc, 8.0000, Long.class, 8l);
		convert(tc, 8718.4, Long.class, 8718l);
		convert(tc, 8718.8, Long.class, 8718l);
		
		convert(tc, 870l, Double.class, 870.0);
	}
	
	@Test
	public void testLongToInteger()
	{
		TypeConverter tc = new TypeConverterImpl(null);
		tc.add(new LongIntegerConversion());
		tc.add(new IntegerLongConversion());
		
		convert(tc, 8, Long.class, 8l);
		convert(tc, Integer.MAX_VALUE, Long.class, (long) Integer.MAX_VALUE);
		
		convert(tc, 8l, Integer.class, 8);
		convert(tc, Integer.MAX_VALUE + 10, Integer.class, -2147483639);
	}
	
	@Test
	public void testLongToShort()
	{
		TypeConverter tc = new TypeConverterImpl(null);
		tc.add(new LongShortConversion());
		tc.add(new ShortLongConversion());
		
		convert(tc, (short) 8, Long.class, 8l);
		convert(tc, Short.MAX_VALUE, Long.class, (long) Short.MAX_VALUE);
		
		convert(tc, 8l, Short.class, (short) 8);
		convert(tc, (long) (Short.MAX_VALUE + 10), Short.class, (short) -32759);
	}
}
