package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class DoubleStringConversion
	implements Conversion<Double, String>
{

	public String convert(Double in)
	{
		return in.toString();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
