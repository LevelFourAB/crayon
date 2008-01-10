package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class VoidLongConversion
	implements Conversion<Void, Long>
{

	public Long convert(Void in)
	{
		return 0l;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
