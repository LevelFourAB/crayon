package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class VoidStringConversion
	implements Conversion<Void, String>
{

	public String convert(Void in)
	{
		return null;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
