package se.l4.crayon.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;

import javax.validation.ValidatorFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.contributions.ContributionsBinder;
import se.l4.crayon.validation.ValidationModule;
import se.l4.exoconf.Config;
import se.l4.exoconf.ConfigException;
import se.l4.exoconf.sources.FileConfigSource;

/**
 * Module that activates support for configuration and makes {@link Config}
 * available.
 */
public class ConfigModule
	implements Module
{
	@Override
	public void configure(Binder binder)
	{
		binder.install(new ValidationModule());

		ContributionsBinder.newBinder(binder)
			.bindContributions(ConfigContribution.class);
	}

	@Provides
	@Singleton
	public Config provideConfig(
		@ConfigContribution Contributions contributions,
		ValidatorFactory validatorFactory
	)
	{
		Config.Builder builder = Config.create()
			.withValidatorFactory(validatorFactory);

		// Find resource files and ask them to be loaded
		builder = readModuleConfigs(builder);

		// Ask the contributions for their configuration
		CollectorImpl collector = new CollectorImpl(builder);
		contributions.run(binder -> binder.bind(ConfigCollector.class).toInstance(collector));
		builder = collector.builder;

		return builder.build();
	}

	/**
	 * Locale all the resources named crayon.conf and load them.
	 *
	 * @param builder
	 * @return
	 */
	private Config.Builder readModuleConfigs(Config.Builder builder)
	{
		try
		{
			Enumeration<URL> confFiles = getClass().getClassLoader().getResources("crayon.conf");
			while(confFiles.hasMoreElements())
			{
				URL url = confFiles.nextElement();
				try(InputStream in = url.openStream())
				{
					FileConfigSource source = FileConfigSource.read(in);
					builder = builder.addSource(source);
				}
				catch(IOException e)
				{
					throw new ConfigException("Tried loading configuration file at " + url + " but failed; " + e.getMessage(), e);
				}
			}
		}
		catch(IOException e)
		{
			throw new ConfigException("Could not find module configuration files; " + e.getMessage(), e);
		}

		return builder;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ConfigModule;
	}

	private static class CollectorImpl
		implements ConfigCollector
	{
		private Config.Builder builder;

		public CollectorImpl(Config.Builder builder)
		{
			this.builder = builder;
		}

		@Override
		public void addFile(File file)
		{
			builder = builder.addFile(file);
		}

		@Override
		public void addFile(Path path)
		{
			builder = builder.addFile(path);
		}

		@Override
		public void addFile(String path)
		{
			builder = builder.addFile(path);
		}

		@Override
		public void addProperty(String key, Object value)
		{
			builder = builder.addProperty(key, value);
		}
	}
}
