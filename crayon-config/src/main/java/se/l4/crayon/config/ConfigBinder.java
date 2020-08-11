package se.l4.crayon.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.util.Types;

import se.l4.exoconf.Config;

/**
 * Binder to help with binding configuration values.
 */
public class ConfigBinder
{
	public interface BindingBuilder<T>
	{
		/**
		 * Bind the configuration value with the given annotation.
		 *
		 * @param annotation
		 * @return
		 */
		BindingBuilder<T> annotatedWith(Class<? extends Annotation> annotation);

		/**
		 * Bind the configuration value with the given annotation.
		 *
		 * @param annotation
		 * @return
		 */
		BindingBuilder<T> annotatedWith(Annotation annotation);

		/**
		 * Set the default value.
		 *
		 */
		BindingBuilder<T> withDefault(T value);

		/**
		 * Set the path in the configuration the object should be retrieved
		 * from.
		 *
		 * @param path
		 * @return
		 */
		ConfigBinder to(String path);
	}

	private final Binder binder;

	private ConfigBinder(Binder binder)
	{
		this.binder = binder.skipSources(ConfigBinder.class, BindingBuilderImpl.class, Definition.class);
	}

	/**
	 * Create a new binder.
	 *
	 * @param binder
	 * @return
	 */
	public static ConfigBinder newBinder(Binder binder)
	{
		binder.install(new ConfigModule());
		return new ConfigBinder(binder);
	}

	/**
	 * Bind the specified type to a configuration value.
	 *
	 * @param key
	 * @param type
	 * @return
	 */
	public <T> BindingBuilder<T> bind(final Class<T> type)
	{
		return new BindingBuilderImpl<T>(this, type);
	}

	private static class BindingBuilderImpl<T>
		implements BindingBuilder<T>
	{
		private final ConfigBinder binder;
		private final Class<T> type;

		private Annotation annotation;
		private Class<? extends Annotation> annotationClass;

		private T defaultValue;

		public BindingBuilderImpl(ConfigBinder binder, Class<T> type)
		{
			this.binder = binder;
			this.type = type;
		}

		@Override
		public ConfigBinder to(String path)
		{
			new Definition(path, type, annotationClass, annotation, defaultValue).bind(binder.binder);

			return binder;
		}

		@Override
		public BindingBuilder<T> annotatedWith(Annotation annotation)
		{
			this.annotation = annotation;

			return this;
		}

		@Override
		public BindingBuilder<T> annotatedWith(Class<? extends Annotation> annotation)
		{
			this.annotationClass = annotation;

			return this;
		}

		@Override
		public BindingBuilder<T> withDefault(T value)
		{
			this.defaultValue = value;

			return this;
		}
	};

	private static class Definition<T>
	{
		private String key;
		private Class<T> type;

		private Class<? extends Annotation> annotationClass;
		private Annotation annotation;

		private T defaultValue;

		public Definition(String key, Class<T> type, Class<? extends Annotation> annotationClass, Annotation annotation, T defaultValue)
		{
			this.key = key;
			this.type = type;
			this.annotationClass = annotationClass;
			this.annotation = annotation;
			this.defaultValue = defaultValue;
		}

		public void bind(Binder binder)
		{

			Type valueType = Types.newParameterizedType(Optional.class, type);
			Key<Optional<T>> valueKey;
			Key<T> key;

			if(annotationClass != null)
			{
				valueKey = (Key) Key.get(valueType, annotationClass);
				key = Key.get(type, annotationClass);
			}
			else if(annotation != null)
			{
				valueKey = (Key) Key.get(valueType, annotation);
				key = Key.get(type, annotation);
			}
			else
			{
				valueKey = (Key) Key.get(valueType);
				key = Key.get(type);
			}

			Provider<Optional<T>> provider = createProvider(binder);

			binder.bind(valueKey)
				.toProvider(provider)
				.in(Scopes.SINGLETON);

			binder.bind(key)
				.toProvider(new ConfigObjectProvider<T>(provider, defaultValue));
		}

		private Provider<Optional<T>> createProvider(Binder binder)
		{
			Provider<Config> config = binder.getProvider(Config.class);
			return new ConfigValueProvider<T>(config, key, type);
		}
	}

	private static class ConfigObjectProvider<T>
		implements Provider<T>
	{
		private final Provider<Optional<T>> provider;
		private final T defaultValue;

		public ConfigObjectProvider(Provider<Optional<T>> provider, T defaultValue)
		{
			this.provider = provider;
			this.defaultValue = defaultValue;
		}

		@Override
		public T get()
		{
			return provider.get().orElse(defaultValue);
		}
	}

	private static class ConfigValueProvider<T>
		implements Provider<Optional<T>>
	{
		private final Provider<Config> config;
		private final String key;
		private final Class<T> type;

		public ConfigValueProvider(Provider<Config> config, String key, Class<T> type)
		{
			this.config = config;
			this.key = key;
			this.type = type;
		}

		@Override
		public Optional<T> get()
		{
			return config.get().get(key, type);
		}
	}
}
