package se.l4.crayon.types;

/**
 * Generic type conversion, used for converting any type to any other type.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TypeConverter
{
	<T> T convert(Object in, Class<T> output);

	void add(Conversion<?, ?> conversion);
}
