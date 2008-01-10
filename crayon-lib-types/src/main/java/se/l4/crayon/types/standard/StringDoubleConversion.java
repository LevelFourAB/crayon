package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;
import se.l4.crayon.types.ConversionException;

public class StringDoubleConversion
	implements Conversion<String, Double>
{

	public Double convert(String in)
	{
		try
		{
			return Double.parseDouble(in);
		}
		catch(NumberFormatException e)
		{
			throw new ConversionException("Invalid double; " + e.getMessage(), e);
		}
	}

	public Class<String> getInput()
	{
		return String.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
