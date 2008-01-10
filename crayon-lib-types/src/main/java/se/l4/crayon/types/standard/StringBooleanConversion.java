package se.l4.crayon.types.standard;

import se.l4.crayon.types.ConversionException;
import se.l4.crayon.types.Conversion;

public class StringBooleanConversion
	implements Conversion<String, Boolean>
{

	public Boolean convert(String in)
	{
		in = in.trim().toLowerCase();
		
		if(in.equals("true") || in.equals("on") || in.equals("1"))
		{
			return true;
		}
		else if(in.equals("false") || in.equals("off") || in.equals("0"))
		{
			return false;
		}
		
		throw new ConversionException("Invalid boolean string: " + in);
	}

	public Class<String> getInput()
	{
		return String.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
