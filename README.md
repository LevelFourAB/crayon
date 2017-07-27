Crayon is an extension to [Google Guice](http://code.google.com/p/google-guice/) that provides the ability to define contribution methods. A contribution method is called after the Guice Injector has been created and can use any binding defined.

The main library is `crayon-core` and is available in Maven-central:

```
<dependency>
  <groupId>se.l4.crayon</groupId>
  <artifactId>crayon-core</artifactId>
  <version>0.7</version>
</dependency>
```

## Defining a contribution

Methods are defined in Guice modules. Either by extending `CrayonModule` or by creating a `CrayonBinder` directly:

```java
public void configure() {
	CrayonBinder.newBinder(binder, this);
}
```

Example of defining a contribution method:

```java
@Contribution
public void contributeService(ServiceManager manager, TestService service) {
  manager.add(service);
}
```

The order at which contributions are invoked can be configured:

```
@Contribution
@Named("service:test")
public void contributeService(ServiceManager manager, TestService service) {
  manager.add(service);
}

@Contribution
@Before("service:test")
public contributeOtherService(ServiceManager manage, OtherService service) {
  // This will run before the contribution named "service:test" 
  manager.add(service);
}
```

## Calling the contributions

Call the contributions by calling `Crayon.start()`:

```java
Injector injector = Guice.createInjector(...);
Crayon crayon = injector.getInstance(Crayon.class);
crayon.start();
```

You can also use `Configurator` to create a your `Injector` in which case
`Crayon.start()` will be called automatically.

```java
Injector injector = new Configurator(stage)
  .add(SomeModule.class)
  .add(new OtherModule())
  .configure();
```

## Custom contributions

It is possible to use any annotation as a marker for a contribution. This is useful when building libraries where more detailed control of contributions is needed. When a custom annotation is used the library author has full control over when the defined methods are run.

First define a custom [binding annotation](http://code.google.com/p/google-guice/wiki/BindingAnnotations). This method is then used instead of `@Contribution` when contributions are defined.

In the consuming module call `bindContributions(Class<? extends Annotation>)` (on either `CrayonModule` or `CrayonBinder`). This will bind an instance of `Contributions` annotated with custom annotation. When desired you can then call the `run` method on the `Contributions` instance.

Example:

```java
@BindingAnnotation
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageContribution
{
}
```

Module that triggers contributions:

```java
public class StorageModule extends CrayonModule {
  public void configure() {
    bindContributions(StorageContribution.class);
    
    // Other configuration here
  }
  
  @Contribution
  public void runStorageContributions(@StorageContribution Contributions contributions) {
    contributions.run();
  }
}

```java
public class StorageExtensionModule extends CrayonModule {
  public void configure() {
  }
  
  @StorageContribution
  public void contributeSomeStorageStuff() {
    // This method is run when the contributions are triggered by StorageModule
  }
}
``` 