package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongShortConversion
	implements Conversion<Long, Short>
{

	public Short convert(Long in)
	{
		return in.shortValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Short> getOutput()
	{
		return Short.class;
	}

}
