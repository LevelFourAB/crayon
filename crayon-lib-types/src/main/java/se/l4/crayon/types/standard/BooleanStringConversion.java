package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class BooleanStringConversion
	implements Conversion<Boolean, String>
{

	public String convert(Boolean in)
	{
		return in.toString();
	}

	public Class<Boolean> getInput()
	{
		return Boolean.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
