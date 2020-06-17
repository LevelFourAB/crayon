# Crayon

Crayon is an application bootstrap that makes it easy to build modular 
applications on top of [Google Guice](https://github.com/google/guice). Crayon
provides access to [logging](#logging), [module discovery](#modules),
[contributions](#contributions), [configuration](#configuration),
[services](#services) and more.

Development version: `2.0.0-SNAPSHOT`

## Getting started

Crayon is split into several sub-projects, depending on your needs different
artifacts need to be imported into your project:

```xml
<dependency>
  <groupId>se.l4.crayon</groupId>
  <artifactId>crayon-app</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

`Application` is the recommended way to start an application built on Crayon.
It will take care of discovery of modules, configure logging and will start any
services defined by modules.

Example of an application using a single module `MainModule`:

```java
public class ServerApp {
  public static void main(String[] args) {
    Application app = Application.withIdentifier("appId")
      .add(MainModule.class)
      .start();   
  }
}
```

## Logging

Logging is provided via [Logback](http://logback.qos.ch/). `Application` will
automatically load the file `logback.xml` if available and will default to
outputting log messages with `INFO`  level to the console if no configuration
is available.

## Modules

**Artifact:** `crayon-module`
**Module:** `se.l4.crayon.module`

Modules in Crayon are built around dependency injection and are an extensions
of Guice modules. With Crayon it is recommended to split a project into several
modules, which if you're using Java 9+ may be JPMS modules as well. 

For each module in your application you should create a class that extends 
`CrayonModule` that describes the classes the module makes available, contributes
things such as [services](#services) and what other modules are required by 
this module.

Here's a module that contributes a service that will start during application
startup:

```java
public class HttpModule extends CrayonModule {
  @Override
  public void configure() {
    // Indicate that we depend on services
    install(new ServicesModule());
  }

  /**
   * This is a contribution method that the crayon-services module will invoke
   * during application startup.
   */
  @ServiceContribution
  public void contributeService(ServiceCollector collector, HttpService service) {
    collector.add(service);
  }
}
```

### Auto-discovery

Crayon supports auto-discovery of modules during startup using [ServiceLoader](https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html). 

When using JPMS modules this can be provided via `module-info.java`:

```
provides se.l4.crayon.module.CrayonModule with com.example.HttpModule;
```

If not using JPMS it can be provided by the file `META-INF/services/se.l4.crayon.module.CrayonModule` 
on the classpath. This file should include the fully qualified class name of
your module:

```
com.example.HttpModule
```

## Contributions

**Artifact:** `crayon-contributions`
**Module:** `se.l4.crayon.contributions`

Contributions is how Crayon wires modules together, contributions are defined
as methods in a module class with an annotation describing the type of
contribution.

It is possible for these methods to have parameters that are provided via injection:

```java
@ExampleContribution
public void contributeThing(ObjectFromGuice thing) {
  ...
}
```

### Ordering

The order at which contributions are invoked can be configured:

```java
@ExampleContribution
@Named("thingA")
public void contributeThingA() {
}

@ExampleContribution
@Before("thingA")
public void contributeThingB() {
  // This will run before the contribution named "thingA" 
}
```

The annotations `Before`, `After` and `Order` can be used for ordering.

### Custom contributions

Custom contributions are bound via annotations. First define a custom 
[binding annotation](https://github.com/google/guice/wiki/BindingAnnotations)
to be used as the contribution annotation:

```java
@BindingAnnotation
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageContribution
{
}
```

In your module call `bindContributions` with your annotation. This will bind an
instance of `Contributions` annotated with custom annotation. When desired you
can then call the `run` method on the `Contributions` instance to run all
the contributions:

```java
public class StorageModule extends CrayonModule {
  public void configure() {
    bindContributions(StorageContribution.class);
    
    // Other configuration here
  }
  
  @Provides
  @Singleton
  public Storage provideStorage(@StorageContribution Contributions contributions) {
	  contributions.run();
	  return new Storage(...);
  }
}
```

Other modules may use your contribution as normal:

```java
public class StorageExtensionModule extends CrayonModule {
  @StorageContribution
  public void contributeSomeStorageStuff() {
    // This method is run when the contributions are triggered by StorageModule
  }
}
``` 

## Configuration

**Artifact:** `crayon-config`
**Module:** `se.l4.crayon.config`

When a Crayon application is started it will automatically load configuration
files and make these available to modules. This is done via [Config](https://javadoc.io/doc/se.l4.commons/commons-config/latest/se/l4/commons/config/Config.html) provided by [L4 commons](https://github.com/levelfourab/commons).

By default the first of `./appId.conf`, `/etc/appId/default.conf` or `./default.conf`
will be loaded.

It is recommended that modules bind up classes via `bindConfig`:

```java
public class ExampleModule extends CrayonModule {
  @Override
  public void configure() {
    bindConfig(ExampleConfig.class)
      .withDefault(new ExampleConfig(8080))
      .to("example.path.in.config");
  }
}

@Use(ReflectionSerializer.class)
public class ExampleConfig {
  @Expose
  @NotEmpty
  private int port;
}
```

Defaults can also be provided via contributions:

```java
@ConfigContribution
public void contributeConfig(ConfigBuilder builder) {
  builder.with("example.path.in.config.port", 8080);
}
```

## Services

**Artifact:** `crayon-services`
**Module:** `se.l4.crayon.services`

## Type discovery and creation

**Artifact:** `crayon-types`
**Module:** `se.l4.crayon.types`

## Health monitoring via Vibe

**Artifact:** `crayon-vibe`
**Module:** `se.l4.crayon.vibe`

Crayon provides integration with [Vibe](https://github.com/levelfourab/vibe)
for health monitoring. By default applications will bind up some JVM health 
metrics under the scope `jvm`.

### Using in modules

Depending on the module `VibeModule` will make an instance of 
[Vibe](https://javadoc.io/doc/se.l4.vibe/vibe-api/latest/se.l4.vibe/se/l4/vibe/Vibe.html)
available for use.

```java
public class ExampleModule extends CrayonModule {
  @Override
  public void configure() {
    // Indicate that this module uses Vibe
    install(new VibeModule());

    // Bind something that uses a Vibe instance
    bind(Thing.class).to(ThingImpl.class);
  }
}
```

### Setting up backends

To make things exported via Vibe useable you will want to bind up one or more
backends depending on your needs. These can be bound via a `@VibeBackendContribution`:

```java
@VibeBackendContribution
public void contributeVibeBackend(Vibe.Builder builder) {
  builder.addBackend(createdBackend);
}
```
