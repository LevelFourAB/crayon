package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class DoubleLongConversion
	implements Conversion<Double, Long>
{

	public Long convert(Double in)
	{
		return in.longValue();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
