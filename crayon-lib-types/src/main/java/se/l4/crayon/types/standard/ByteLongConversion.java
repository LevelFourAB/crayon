package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class ByteLongConversion
	implements Conversion<Byte, Long>
{

	public Long convert(Byte in)
	{
		return in.longValue();
	}

	public Class<Byte> getInput()
	{
		return Byte.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
