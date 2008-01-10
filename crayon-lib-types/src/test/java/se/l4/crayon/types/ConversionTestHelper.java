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
