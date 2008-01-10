package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class BooleanLongConversion
	implements Conversion<Boolean, Long>
{

	public Long convert(Boolean in)
	{
		return in.booleanValue() ? 1l : 0l;
	}

	public Class<Boolean> getInput()
	{
		return Boolean.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
