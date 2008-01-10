package se.l4.crayon.types;

/**
 * Compound conversion used for chaining two type conversions together.
 *  
 * @author Andreas Holstenson
 *
 */
public class CompoundTypeConversion
	implements Conversion<Object, Object>
{
	private final Conversion<Object, Object> in;
	private final Conversion<Object, Object> out;
	
	public CompoundTypeConversion(Conversion<Object, Object> in, 
			Conversion<Object, Object> out)
	{
		this.in = in;
		this.out = out;
	}
	
	public Object convert(Object in)
	{
		Object firstPass = this.in.convert(in);
		return out.convert(firstPass);
	}

	public Class<Object> getInput()
	{
		return in.getInput();
	}

	public Class<Object> getOutput()
	{
		return out.getOutput();
	}

}
