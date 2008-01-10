package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class FloatDoubleConversion
	implements Conversion<Float, Double>
{

	public Double convert(Float in)
	{
		return in.doubleValue();
	}

	public Class<Float> getInput()
	{
		return Float.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
