package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongDoubleConversion
	implements Conversion<Long, Double>
{

	public Double convert(Long in)
	{
		return in.doubleValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
