package se.l4.crayon.app.internal;

import java.util.Optional;

import com.google.inject.Stage;

/**
 * Helper to get properties from either the JVM properties or the environment.
 */
public class PropertiesHelper
{
	private PropertiesHelper()
	{
	}

	public static Optional<String> get(String jdkName, String envName)
	{
		String value = System.getProperty(jdkName);
		if(value != null && ! value.trim().isEmpty())
		{
			return Optional.of(value.trim());
		}

		value = System.getenv(envName);
		if(value != null && ! value.trim().isEmpty())
		{
			return Optional.of(value.trim());
		}

		return Optional.empty();
	}

	/**
	 * Get the default stage. This will look for a system property named
	 * production that can be set to false to run in development mode.
	 *
	 * @return
	 */
	public static Stage getDefaultStage()
	{
		Optional<String> stage = get("stage", "STAGE");
		if(! stage.isPresent())
		{
			return Stage.PRODUCTION;
		}


		switch(stage.get().toLowerCase())
		{
			case "development":
			case "dev":
				return Stage.DEVELOPMENT;
			default:
				return Stage.PRODUCTION;
		}
	}
}
