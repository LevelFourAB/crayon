package se.l4.crayon.app.internal;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.LogManager;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import se.l4.crayon.app.Application;
import se.l4.crayon.app.Application.Builder;
import se.l4.crayon.app.ApplicationException;
import se.l4.crayon.config.ConfigModule;
import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.ServicesModule;

public class ApplicationBuilder
	implements Application.Builder
{
	private final String identifier;
	private final Set<Module> modules;

	public ApplicationBuilder(String identifier)
	{
		this.identifier = identifier;

		modules = new HashSet<>();
	}

	@Override
	public Builder add(Class<? extends CrayonModule> module)
	{
		try
		{
			CrayonModule m = module.getDeclaredConstructor().newInstance();
			return add(m);
		}
		catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException| InstantiationException | IllegalArgumentException | SecurityException e)
		{
			throw new ApplicationException("Unable to create module, a public no-args constructor required; " + e.getMessage(), e);
		}
	}

	@Override
	public Builder add(CrayonModule module)
	{
		modules.add(module);
		return this;
	}

	@Override
	public Application start()
	{
		configureLogging();

		Stage stage = PropertiesHelper.getDefaultStage();

		// Resolve the configuration and make it available
		List<Path> configFiles = new ArrayList<>();
		findDefaultConfigFile().ifPresent(configFiles::add);
		modules.add(new ConfigModule(configFiles));

		// Make sure services are available
		modules.add(new ServicesModule());

		// Load modules registered via ServiceLoader
		ServiceLoader.load(CrayonModule.class).forEach(modules::add);

		// Create the injector
		Injector injector = Guice.createInjector(stage, modules);

		// Create the application and start the services
		ApplicationImpl result = new ApplicationImpl(injector);
		result.startServices();
		return result;
	}

	/**
	 * Configure logging of application.
	 */
	private void configureLogging()
	{
		// Setup that SLF4J should handle Java Logging
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();

		// Try to locate logging config
		Optional<Path> file = findConfigFile("logback.xml");

		if(file.isPresent())
		{
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

			try
			{
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				context.reset();
				configurator.doConfigure(file.get().toFile());
			}
			catch(JoranException e)
			{
				configureDefaultLogging();
			}
		}
		else
		{
			configureDefaultLogging();
		}
	}

	/**
	 * Configure the default logging. Logback falls back on console logging
	 * but will default to debug level so change this to info level to make
	 * things cleaner.
	 */
	private void configureDefaultLogging()
	{
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		// Default to info level if no configuration is available
		context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
	}

	/**
	 * Find the default configuration file to use.
	 *
	 * @return
	 */
	private Optional<Path> findDefaultConfigFile()
	{
		return findBestConfigFile(
			PropertiesHelper.get("configFile", "CONFIG_FILE").orElse(null),
			identifier + ".conf",
			"default.conf",
			"/etc/" + identifier + "/default.conf"
		);
	}

	protected Optional<Path> findConfigFile(String fileName)
	{
		return findBestConfigFile(
			fileName,
			"/etc/" + identifier + "/" + fileName
		);
	}

	private Optional<Path> findBestConfigFile(String... paths)
	{
		return Arrays.stream(paths)
			.filter(s -> s != null)
			.map(Paths::get)
			.filter(Files::exists)
			.findFirst();
	}
}
