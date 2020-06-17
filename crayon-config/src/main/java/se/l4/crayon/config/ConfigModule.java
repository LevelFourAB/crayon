package se.l4.crayon.config;

import java.nio.file.Path;

import javax.validation.Validation;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.commons.config.Config;
import se.l4.commons.config.ConfigBuilder;
import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.contributions.ContributionsBinder;

public class ConfigModule
	implements Module
{
	private Iterable<Path> files;

	/**
	 * Create a new module that will bind a {@link Config} instance.
	 *
	 * @param files
	 */
	public ConfigModule(Iterable<Path> files)
	{
		this.files = files;
	}

	@Override
	public void configure(Binder binder)
	{
		ContributionsBinder.newBinder(binder)
			.bindContributions(ConfigContribution.class);
	}

	@Provides
	@Singleton
	public Config provideConfig(
		@ConfigContribution Contributions contributions
	)
	{
		ConfigBuilder builder = Config.builder()
			.withValidatorFactory(Validation.buildDefaultValidatorFactory());

		contributions.run(binder -> binder.bind(ConfigBuilder.class).toInstance(builder));

		for(Path p : files)
		{
			builder.addFile(p);
		}

		files = null;

		return builder.build();
	}
}
