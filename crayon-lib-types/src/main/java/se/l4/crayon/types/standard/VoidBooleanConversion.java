package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class VoidBooleanConversion
	implements Conversion<Void, Boolean>
{

	public Boolean convert(Void in)
	{
		return false;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
