package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongIntegerConversion
	implements Conversion<Long, Integer>
{

	public Integer convert(Long in)
	{
		return in.intValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Integer> getOutput()
	{
		return Integer.class;
	}

}
