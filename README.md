Akka Guice Integration
=======================

Installation
------------

Add the following to your build.sbt:

```java
resolvers += "release repository" at "http://chanan.github.io/maven-repo/releases/"

resolvers += "snapshot repository" at "http://chanan.github.io/maven-repo/snapshots/"
```

Add to your libraryDependencies:

```java
"akkaguice" %% "akkaguice" % "0.8.3"
```

Initialization
--------------

First, create or edit conf/play.plugins. Add the line (The number in front of the plugin is the load order inside your play app):

```
10000:akkaGuice.AkkaGuicePlugin
```

In Global.java create an Injector using AkkaGuiceModule. You may also
pass in your own GuiceModules, as in the example below. 
Next, in the onStart callback method pass the injector to AkkaGuice.InitializeInjector():

```java
public class Global extends GlobalSettings {
	private Injector injector;

	public <A> A getControllerInstance(Class<A> clazz) throws Exception {
		return injector.getInstance(clazz);
	}

	@Override
	public void onStart(Application arg0) {
        injector = Guice.createInjector(new AkkaGuiceModule(), new GuiceModule());
		AkkaGuice.InitializeInjector(injector);
	}
}
```

Usage
-----

### Registering Actors

AkkaGuice will scan your application code. This will make Actors available in Guice 
to be injected into your controllers or services.

### Naming an Actor

AkkaGuice will be default register your actors by the class name. If a collision occurs, it will use the fully qualified 
class name of the actor. An example of this is the 
HelloActor in the sample project. There is one registered as services.HelloActor and one as services.schedule.HelloActor. 
You may also name your actor using @Named annotation (java.inject.Named).

For example:

```java
@Named("AnnotatedActor") @Singleton
public class AnnotatedWithNameActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor using named annotation");
	}
}
```

### Top Level Actors Versus Per Request Actors

An actor marked with the @Singleton annotation will return the same ActorRef from Guice. If the Actor is not annotated
as a @Singleton, a new ActorRef will be returned each time. Also, a non-singleton actor will be registered in AkkaGuice's 
PropsContext (See below: "On Demand Creation of Actors").

### A Note About Usage of Singleton

You may have seen the [warning in the akka documentation in the Dependancy Injection section](http://doc.akka.io/docs/akka/2.2.3/java/untyped-actors.html#Dependency_Injection)
regarding using Singleton attribute. The warning applies to returning the same actor class. In the case of AkkaGuice we are
returning the same ActorRef. This still allows Akka to restart the actor if needed.

### Actors in Controllers

As mention above, when the annotation does not have a name, Actors are bound to ActorRefs with a name of the class 
if there are no collision. Otherwise they are bound to a name of the fully qualified class name. 
For example, in the sample app in the Application controller the HelloActor is injected with the fully qualified 
name as two HelloActors exist in the project:

```java
private final ActorRef service;
	
@Inject
public Application(@Named("services.HelloActor") ActorRef service) {
	this.service = service;
}
```

### Services in Actors

Any services injected into actors will be resolved as well:

```java
public class HelloActor extends UntypedActor {
private final SayHello hello;

	@Inject
 	public HelloActor(SayHello hello) {
		this.hello = hello;
 	}

	...
}
```

### On Demand Creation of Actors

Per Request ActorRefs can also be requested from Guice on demand. All injections will still be resolved. 
This example is from services.HelloActor:

```java
final ActorRef perRequestActor = Akka.system().actorOf(PropsContext.get(PerRequestActor.class));
```

Or:

```java
final ActorRef perRequestActorByName = getContext().actorOf(PropsContext.get("PerRequest"));
```

Scheduling
---------

AkkaGuice also provides for automatic scheduling of Actors. A String "tick" will be sent 
to the Actor on the schedule set by the annotation. 

### Schedule

As seen in the services.schedule.HelloActor class, use the schedule annotation to 
periodically ping your actor:

```java
@Schedule(initialDelay = 1, timeUnit = TimeUnit.SECONDS, interval = 2)
```

### Schedule Once

To ping your actor one time use the ScheduleOnce annotation. This example is located in 
services.schedule.HelloOnceActor:

```java
@ScheduleOnce()
```

In this case, the defaults of the annotation were used:

* InitialDelay: 500
* TimeUnit: MILLISECONDS

Scheduling via Conf File
------------------------

If values are entered in the conf file they override the values of the annotation.

### Schedule

The following entries can be entered in the conf file:

```java
services.schedule.HelloRepeatConfigActor.initialDelay = 4 seconds
services.schedule.HelloRepeatConfigActor.interval = 2 seconds
```

### Schedule once

The following can be entered via config:

```java
services.schedule.HelloOnceConfigActor.initialDelay = 5 seconds
```

### Enable via config

You can disable both types of scheduled actors via config:

```java
services.schedule.NotEnabledActor.enabled = false
```

Release History
---------------

* 0.8.2 - Fixed an incompatibility with Ebean and changed the module to a plugin
* 0.8.1 - Compatible with Java 7 and 8
* 0.8.0 - Updated to support AbstractActor in Akka 2.3
* 0.7.1 - Actors created with akka guice will get a name based on the key
* 0.7.0 - Removed @RegisterActor in favor of @Singleton & @Named
* 0.6.0 - Change the API to not require child injectors
* 0.5.0 - Scheduling via conf files
* 0.4.0 - Added: RegisterProps and PropsContext
* 0.3.0 - Changed to not scan class automatically based on feedback on the Akka Google group.
* 0.2.0 - Added Named annotation
* 0.1.0 - Initial release
