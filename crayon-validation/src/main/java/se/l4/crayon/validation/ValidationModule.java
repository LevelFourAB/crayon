package se.l4.crayon.validation;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.contributions.ContributionsBinder;

/**
 * Module that activates support for bean validation via
 * {@link javax.validation.ValidatorFactory}.
 */
public class ValidationModule
	implements Module
{
	@Override
	public void configure(Binder binder)
	{
		ContributionsBinder.newBinder(binder)
			.bindContributions(ValidationContribution.class);
	}

	@Provides
	@Singleton
	public ValidatorFactory provideValidatorFactory(
		@ValidationContribution Contributions contributions
	)
	{
		GenericBootstrap bootstrap = Validation.byDefaultProvider();
		Configuration<?> configuration = bootstrap.configure();

		contributions.run(binder -> binder.bind(Configuration.class).toInstance(configuration));

		return configuration.buildValidatorFactory();
	}

	@Provides
	public Validator provideValidator(ValidatorFactory factory)
	{
		return factory.getValidator();
	}
}
