package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class VoidDoubleConversion
	implements Conversion<Void, Double>
{

	public Double convert(Void in)
	{
		return 0.0;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
