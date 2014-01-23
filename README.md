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
"akkaguice" %% "akkaguice" % "0.4.0"
```

Initialization
--------------

In Global.java create an Injector as a non final variable. In the onStart callback method pass the injector to AkkaGuice and your namespace (such as com.company.project - In this case I am using the "services" package):

```java
@Override
public void onStart(Application arg0) {
 	injector = AkkaGuice.Startup(injector, "services");
}
```

Usage
-----

### Registering "Top Level" Actors

Annotate "top level" actors with @RegisterActor. This will make them available in Guice to be injected into your controllers.
If no value is provided to the annotation the ActorRef will be registered with the class name unless there is a collision. 
In that case it will be registered with the fully qualified class name. Optionally, you may register an actor with a name.
For example:

```java
@RegisterActor("AnnotatedActor")
public class AnnotatedWithNameActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor using named annotation");
	}
}
```

### Actors in Controllers

As mention above, when the annotation does not have a name, Actors are bound to ActorRefs with a name of the class 
if there are no collision. Otherwise they are bound to a Name of the fully qualified name. 
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

ActorRefs can also be request from Guice on demand. All injections will still be resolved. This example is from services.HelloActor:

```java
final ActorRef perRequestActor = Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(PerRequestActor.class));
```

### Registering Props for On Demand Actors

With the @RegisterProps annotation, Props for on demand actors can also be registered in PropsContext. For example instead of the line above, one could annotate the PerRequestActor class:

```java
@@RegisterProps("PerRequest")
public class PerRequestActor extends UntypedActor {

}
```

And then use either:

```java
final ActorRef perRequestActorByClass = getContext().actorOf(PropsContext.get(PerRequestActor.class));
perRequestActorByClass.tell("tick", getSelf());
```

Or:

```java
final ActorRef perRequestActorByName = getContext().actorOf(PropsContext.get("PerRequest"));
perRequestActorByName.tell("tick", getSelf());
```java

Scheduling
---------

AkkaGuice also provides for automatic scheduling of Actors. A String "tick" will be sent to the Actor on the schedule set by the annotation. 

### Schedule

As seen in the services.schedule.HelloActor class, use the schedule annotation to periodically ping your actor:

```java
@Schedule(initialDelay = 1, timeUnit = TimeUnit.SECONDS, interval = 2)
```

### Schedule Once

To ping your actor one time use the ScheduleOnce annotation. This example is located in services.schedule.HelloOnceActor:

```java
@ScheduleOnce()
```

In this case, the defaults of the annotation were used:

* InitialDelay: 500
* TimeUnit: MILLISECONDS

Limitation
----------

Due to the way I integrated Akka & Guice together, by using Guice child injectors, the order of registration is important.
Normally, you use one AbstractModule to register all your registrations in them. Due to this Guice is able to resolve them all
no matter the order. Because AkkaGuice registers all the actors after you first create a module, any classes that use those actors
will not resolve. Due to this, you may need to define two AbstractModules. The first, will contain all your services that any
of your actors may require (Or you may also leave it blank). The second will contain any services that rely on those actors.

An example of this can be seen in the class: Services.ServiceThatUsesActorImpl. It is defined in an AbstractModule named
ServicesThatUseActorsModule. If the service registration were to be moved to GuiceModule it would trigger a runtime exception.
In the OnStart method of Global, you need to call the new AbstractModule:

```java
@Override
public void onStart(Application arg0) {
	injector = AkkaGuice.Startup(injector, "services");
	injector = injector.createChildInjector(new ServicesThatUseActorsModule());
}
```

Final Note
----------

ActorRefs are registered in Guice as an instance. This means that when using the injection syntax, you will be getting the same ActorRef every time. If you need to get a new ActorRef such as when using the Actor Per Request pattern use the On Demand syntax. To see the difference in behaviour run the sample application browse to http://localhost:9000 (this page will be displayed) and refresh the page. In the console log you will see output similar to this:

```
[info] application - Say hello once!
[info] application - Hello from InjectedActorIntoService that was injected into ServiceThatUsesActor
[info] application - Hello from actor using named annotation
[info] application - Hello from actor: Actor[akka://application/user/services.HelloActor#-373722303]
[info] application - Hello from service. Being called from: Actor[akka://application/user/services.HelloActor#-373722303]
[info] application - Hello from per request actor: Actor[akka://application/user/services.HelloActor/$a#-1180069760]
[info] application - Hello from service. Being called from: Actor[akka://application/user/services.HelloActor/$a#-1180069760]
[info] application - Hello from schedule package every 2 seconds
[info] application - Hello from InjectedActorIntoService that was injected into ServiceThatUsesActor
[info] application - Hello from actor: Actor[akka://application/user/services.HelloActor#-373722303]
[info] application - Hello from actor using named annotation
[info] application - Hello from service. Being called from: Actor[akka://application/user/services.HelloActor#-373722303]
[info] application - Hello from per request actor: Actor[akka://application/user/services.HelloActor/$b#374554909]
[info] application - Hello from service. Being called from: Actor[akka://application/user/services.HelloActor/$b#374554909]
[info] application - Hello from schedule package every 2 seconds
```

On line 1 we see the message from HelloActor that was injected into the Application controller. On the 3rd line we see a message from the on demand actor. Hitting refresh causes lines 7 and 9 to be printed. Looking at lines 1 and 7 we can see that the Actors are the same. Looking at the per request actors we can see that they are not the same ActorRef. 

Release History
---------------

* 0.4.0 - Added: RegisterProps and PropsContext
* 0.3.0 - Changed to not scan class automatically based on feedback on the Akka Google group.
* 0.2.0 - Added Named annotation
* 0.1.0 - Initial release
