package se.l4.crayon.module;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;

import se.l4.commons.config.Config;
import se.l4.crayon.config.ConfigBinder;
import se.l4.crayon.contributions.Contributions;
import se.l4.crayon.contributions.ContributionsBinder;

/**
 * Module for use within a Crayon application. Activates support for
 * {@link ContributionsBinder contributions} and {@link ConfigBinder configuration}.
 */
public abstract class CrayonModule
	implements Module
{
	private Binder binder;
	private ContributionsBinder contributions;
	private ConfigBinder configBinder;

	@Override
	public final synchronized void configure(Binder builder)
	{
		binder = setupBinder(
			builder.skipSources(CrayonModule.class)
		);
		contributions = ContributionsBinder.newBinder(binder, this);

		try
		{
			configure();
		}
		finally
		{
			binder = null;
			contributions = null;
		}
	}

	/**
	 * Setup the given binder for use within this module. This is intended
	 * for abstract subclasses of this module so that they can use things
	 * such as {@link Binder#skipSources(Class...)}.
	 *
	 * @param binder
	 *   the binder to setup
	 * @return
	 *   the binder used by the module
	 */
	protected Binder setupBinder(Binder binder)
	{
		return binder;
	}

	/**
	 * Perform configuration.
	 */
	protected void configure()
	{
	}

	/**
	 * Get the {@link Binder} in use.
	 */
	protected Binder binder()
	{
		return binder;
	}

	/**
	 * Get the {@link ContributionsBinder} for this module.
	 *
	 * @return
	 */
	protected ContributionsBinder contributionsBinder()
	{
		return contributions;
	}

	/**
	 * Bind a {@link Contributions} instance. It will be bound so that the
	 * instance is annotated with the specified annotation.
	 *
	 * <p>
	 * For example, calling this:
	 *
	 * <pre>
	 * bindContributions(Test.class)
	 * </pre>
	 *
	 * you can later access the instance via
	 * <p>
	 * class TestClass {
	 * 	public TestClass({@literal @Test} Contributions contributions) {
	 * 		...
	 * 	}
	 * }
	 * </p>
	 *
	 * @see ContributionsBinder#bindContributions(Class)
	 * @param annotation
	 */
	protected void bindContributions(Class<? extends Annotation> annotation)
	{
		contributions.bindContributions(annotation);
	}

	/**
	 * Get the configuration binder for this module.
	 *
	 * @return
	 *   instance of config binder
	 */
	protected ConfigBinder configBinder()
	{
		if(configBinder == null)
		{
			requireBinding(Config.class);
			configBinder = ConfigBinder.newBinder(binder());
		}

		return configBinder;
	}

	/**
	 * Start binding a configuration value of the given type.
	 */
	protected <T> ConfigBinder.BindingBuilder<T> bindConfig(Class<T> configType)
	{
		return configBinder().bind(configType);
	}

	/**
	 * @see Binder#bindScope(Class, Scope)
	 */
	protected void bindScope(Class<? extends Annotation> scopeAnnotation,
			Scope scope)
	{
		binder.bindScope(scopeAnnotation, scope);
	}

	/**
	 * @see Binder#bind(Key)
	 */
	protected <T> LinkedBindingBuilder<T> bind(Key<T> key)
	{
		return binder.bind(key);
	}

	/**
	 * @see Binder#bind(TypeLiteral)
	 */
	protected <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral)
	{
		return binder.bind(typeLiteral);
	}

	/**
	 * @see Binder#bind(Class)
	 */
	protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz)
	{
		return binder.bind(clazz);
	}

	/**
	 * @see Binder#bindConstant()
	 */
	protected AnnotatedConstantBindingBuilder bindConstant()
	{
		return binder.bindConstant();
	}

	/**
	 * @see Binder#install(Module)
	 */
	protected void install(Module module)
	{
		binder.install(module);
	}

	/**
	 * @see Binder#addError(String, Object[])
	 */
	protected void addError(String message, Object... arguments)
	{
		binder.addError(message, arguments);
	}

	/**
	 * @see Binder#addError(Throwable)
	 */
	protected void addError(Throwable t)
	{
		binder.addError(t);
	}

	/**
	 * @see Binder#addError(Message)
	 */
	protected void addError(Message message)
	{
		binder.addError(message);
	}

	/**
	 * @see Binder#requestInjection(Object)
	 */
	protected void requestInjection(Object instance)
	{
		binder.requestInjection(instance);
	}

	/**
	 * @see Binder#requestStaticInjection(Class[])
	 */
	protected void requestStaticInjection(Class<?>... types)
	{
		binder.requestStaticInjection(types);
	}

	/**
	 * Adds a dependency from this module to {@code key}. When the injector is
	 * created, Guice will report an error if {@code key} cannot be injected.
	 * Note that this requirement may be satisfied by implicit binding, such as
	 * a public no-arguments constructor.
	 */
	protected void requireBinding(Key<?> key)
	{
		binder.getProvider(key);
	}

	/**
	 * Adds a dependency from this module to {@code type}. When the injector is
	 * created, Guice will report an error if {@code type} cannot be injected.
	 * Note that this requirement may be satisfied by implicit binding, such as
	 * a public no-arguments constructor.
	 */
	protected void requireBinding(Class<?> type)
	{
		binder.getProvider(type);
	}

	/**
	 * @see Binder#getProvider(Key)
	 */
	protected <T> Provider<T> getProvider(Key<T> key)
	{
		return binder.getProvider(key);
	}

	/**
	 * @see Binder#getProvider(Class)
	 */
	protected <T> Provider<T> getProvider(Class<T> type)
	{
		return binder.getProvider(type);
	}

	/**
	 * @see Binder#convertToTypes
	 */
	protected void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher,
			TypeConverter converter)
	{
		binder.convertToTypes(typeMatcher, converter);
	}

	/**
	 * @see Binder#currentStage()
	 */
	protected Stage currentStage()
	{
		return binder.currentStage();
	}

	/**
	 * @see Binder#getMembersInjector(Class)
	 */
	protected <T> MembersInjector<T> getMembersInjector(Class<T> type)
	{
		return binder.getMembersInjector(type);
	}

	/**
	 * @see Binder#getMembersInjector(TypeLiteral)
	 */
	protected <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> type)
	{
		return binder.getMembersInjector(type);
	}

	/**
	 * @see Binder#bindListener(com.google.inject.matcher.Matcher, com.google.inject.spi.TypeListener)
	 */
	protected void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher,
			TypeListener listener)
	{
		binder.bindListener(typeMatcher, listener);
	}

	/**
	 * @see Binder#bindListener(Matcher, ProvisionListener...)
	 */
	protected void bindListener(Matcher<? super Binding<?>> bindingMatcher, ProvisionListener... listener)
	{
		binder().bindListener(bindingMatcher, listener);
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj.getClass() == getClass();
	}
}
