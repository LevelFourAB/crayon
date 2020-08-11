package se.l4.crayon.config;

import java.io.File;
import java.nio.file.Path;

public interface ConfigCollector
{
	/**
	 * Add a configuration file to the configuration.
	 *
	 * @param path
	 */
	void addFile(Path path);

	/**
	 * Add a configuration file to the configuration.
	 *
	 * @param path
	 */
	void addFile(String path);

	/**
	 * Add a configuration file to the configuration.
	 *
	 * @param file
	 */
	void addFile(File file);

	/**
	 * Add a manually defined property value.
	 *
	 * @param key
	 * @param value
	 */
	void addProperty(String key, Object value);
}
