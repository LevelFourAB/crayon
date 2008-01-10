package se.l4.crayon.types;

/**
 * Conversion between two types, used by the {@link TypeConverter} to perform
 * actual type conversions.
 * 
 * @author Andreas Holstenson
 *
 * @param <I>
 * 		input type
 * @param <O>
 * 		output type
 */
public interface Conversion<I, O>
{
	/**
	 * Convert the given input.
	 * 
	 * @param in
	 * 		input
	 * @return
	 * 		converted output
	 * @throws ConversionException
	 * 		if unable to convert input
	 */
	O convert(I in);
	
	/**
	 * Get class of input (should match {@code <I>} parameter).
	 * 
	 * @return
	 */
	Class<I> getInput();
	
	/**
	 * Get class of output (should match {@code <O>} parameter).
	 * 
	 * @return
	 */
	Class<O> getOutput();
}
