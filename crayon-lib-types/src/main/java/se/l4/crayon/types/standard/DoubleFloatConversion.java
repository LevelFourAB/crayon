package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class DoubleFloatConversion
	implements Conversion<Double, Float>
{

	public Float convert(Double in)
	{
		return in.floatValue();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<Float> getOutput()
	{
		return Float.class;
	}

}
