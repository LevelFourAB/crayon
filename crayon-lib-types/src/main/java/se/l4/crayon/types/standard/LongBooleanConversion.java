package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongBooleanConversion
	implements Conversion<Long, Boolean>
{

	public Boolean convert(Long in)
	{
		return in.longValue() == 1 ? true : false;
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
