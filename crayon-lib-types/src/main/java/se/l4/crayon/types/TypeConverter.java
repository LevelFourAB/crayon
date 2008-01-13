package se.l4.crayon.types;

/**
 * Generic type conversion, used for converting any type to any other type.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TypeConverter
{
	/**
	 * Convert the given input to another type.
	 * 
	 * @param <T>
	 * 		type of output
	 * @param in
	 * 		value to convert (input)
	 * @param output
	 * 		output type
	 * @return
	 * 		converted value
	 * @throws ConversionException
	 * 		if unable to convert
	 */
	<T> T convert(Object in, Class<T> output);

	/**
	 * Add a conversion between two types.
	 * 
	 * @param conversion
	 * 		conversion
	 */
	void add(Conversion<?, ?> conversion);
	
	void add(Class<? extends Conversion<?, ?>> conversion);
}
