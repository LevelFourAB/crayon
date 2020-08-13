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

import com.google.inject.Binder;
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
import se.l4.crayon.config.ConfigCollector;
import se.l4.crayon.config.ConfigContribution;
import se.l4.crayon.config.ConfigModule;
import se.l4.crayon.module.CrayonModule;
import se.l4.crayon.services.ServiceStatus;
import se.l4.crayon.services.ServicesModule;
import se.l4.crayon.vibe.VibeModule;

public class ApplicationBuilder
	implements Application.Builder
{
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

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

		logger.info("[1/5] Finding configuration files");

		List<Path> configFiles = new ArrayList<>();
		findDefaultConfigFile().ifPresent(configFiles::add);

		if(configFiles.isEmpty())
		{
			logger.info("  No configuration files");
		}
		else
		{
			configFiles.forEach(f -> logger.info("  " + f));
		}

		logger.info("[2/5] Discovering modules");
		modules.forEach(m -> logger.info("  Using module " + m.getClass().getName()));

		Stage stage = PropertiesHelper.getDefaultStage();

		// Resolve the configuration and make it available
		modules.add(new ConfigFilesModule(configFiles));

		// Make sure services are available
		modules.add(new ServicesModule());

		// Health metrics via Vibe
		modules.add(new VibeModule());

		// Load modules registered via ServiceLoader
		ServiceLoader.load(CrayonModule.class).forEach(m -> {
			modules.add(m);
			logger.info("  Using auto-discovered module " + m.getClass().getName());
		});

		logger.info("[3/5] Initializing modules and creating injector");

		// Create the injector
		Injector injector = Guice.createInjector(stage, modules);

		// Create the application and start the services
		ApplicationImpl result = new ApplicationImpl(injector);
		logger.info("[4/5] Starting services");

		List<ServiceStatus> services = result.services.startAll()
			.collectSortedList((a, b) -> a.getService().toString().compareTo(b.getService().toString()))
			.block();

		logger.info("[5/5] Startup done");
		if(services.isEmpty())
		{
			logger.info("  No services");
		}
		else
		{
			for(ServiceStatus info : services)
			{
				logger.info(String.format("  [ %-8s ] %s", info.getState(), info.getService()));
			}
		}

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

	private static class ConfigFilesModule
		implements Module
	{
		private final List<Path> files;

		public ConfigFilesModule(List<Path> files)
		{
			this.files = files;
		}

		@Override
		public void configure(Binder binder)
		{
			binder.install(new ConfigModule());
		}

		@ConfigContribution
		public void contributeConfigFiles(
			ConfigCollector collector
		)
		{
			for(Path path : files)
			{
				collector.addFile(path);
			}
		}
	}
}
