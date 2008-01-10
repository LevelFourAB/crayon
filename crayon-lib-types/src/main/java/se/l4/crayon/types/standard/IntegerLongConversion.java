package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class IntegerLongConversion
	implements Conversion<Integer, Long>
{

	public Long convert(Integer in)
	{
		return in.longValue();
	}

	public Class<Integer> getInput()
	{
		return Integer.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
