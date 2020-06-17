package se.l4.crayon.vibe;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.module.CrayonModule;
import se.l4.vibe.Vibe;

/**
 * Module that activates support for health monitoring via {@link Vibe}.
 */
public class VibeModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bindContributions(VibeBackendContribution.class);
	}

	@Provides
	@Singleton
	public Vibe provideVibe(
		@VibeBackendContribution Contributions contributions
	)
	{
		Vibe.Builder builder = Vibe.builder();

		contributions.run(binder -> binder.bind(Vibe.Builder.class).toInstance(builder));

		Vibe vibe = builder.build();
		return vibe;
	}
}
