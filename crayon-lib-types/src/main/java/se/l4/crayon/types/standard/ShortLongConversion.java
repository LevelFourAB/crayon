package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class ShortLongConversion
	implements Conversion<Short, Long>
{

	public Long convert(Short in)
	{
		return in.longValue();
	}

	public Class<Short> getInput()
	{
		return Short.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
