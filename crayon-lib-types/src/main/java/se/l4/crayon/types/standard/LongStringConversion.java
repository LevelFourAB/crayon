package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongStringConversion
	implements Conversion<Long, String>
{

	public String convert(Long in)
	{
		return in.toString();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
