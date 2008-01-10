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
