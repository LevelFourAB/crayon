package se.l4.crayon.types.standard;

import se.l4.crayon.types.Conversion;

public class LongByteConversion
	implements Conversion<Long, Byte>
{

	public Byte convert(Long in)
	{
		return in.byteValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Byte> getOutput()
	{
		return Byte.class;
	}

}
