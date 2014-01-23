package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.util.StringUtils;

import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;
import akkaGuice.annotations.RegisterProps;
import akkaGuice.annotations.Schedule;
import akkaGuice.annotations.ScheduleOnce;

import com.google.inject.Binder;
import com.google.inject.name.Names;

class ActorScanner {
	public static void ScanForActors(Binder binder, String... namespaces) {
		Logger.debug("Actor Scanner Started...");
		RegisterActors(binder, namespaces);
		RegisterProps(namespaces);
		ScheduleActors(namespaces);
		ScheduleOnceActors(namespaces);
	}
	
	private static void RegisterActors(Binder binder, String... namespaces) {
		final Map<String, Class<? extends UntypedActor>> map = new HashMap<>();		
		final ConfigurationBuilder configBuilder = build(namespaces);
		final Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));
		final Set<Class<?>> actors = reflections.getTypesAnnotatedWith(RegisterActor.class);	
		for(final Class<?> potentialActor : actors) {
			final Class<? extends UntypedActor> actor = potentialActor.asSubclass(UntypedActor.class);
			final RegisterActor annotation = actor.getAnnotation(RegisterActor.class);
			if(!StringUtils.isEmpty(annotation.value())) {
				map.put(annotation.value(), actor);
			} else {
				if(map.containsKey(actor.getSimpleName())){
					map.put(actor.getName(), actor);
					final Class<? extends UntypedActor> tempActor = map.remove(actor.getSimpleName());
					map.put(tempActor.getName(), tempActor);
				}
				else map.put(actor.getSimpleName(), actor);
			}
		}
		if(!map.isEmpty()) Logger.debug("Registering actors: ");
		for(final String key : map.keySet()) {
			final Class<? extends UntypedActor> actor = map.get(key);
			binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toInstance(Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor), key));
			Logger.debug("class " + actor.getSimpleName() + " to name: " + key);
		}
	}
	
	private static void RegisterProps(String... namespaces) {
		final ConfigurationBuilder configBuilder = build(namespaces);
		final Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));
		final Set<Class<?>> actors = reflections.getTypesAnnotatedWith(RegisterProps.class);	
		for(final Class<?> potentialActor : actors) {
			final Class<? extends Actor> actor = potentialActor.asSubclass(Actor.class);
			final RegisterProps annotation = actor.getAnnotation(RegisterProps.class);
			if(!StringUtils.isEmpty(annotation.value())) {
				PropsContext.put(annotation.value(), actor);
			} else {
				if(PropsContext.containsKey(actor.getSimpleName())){
					PropsContext.put(actor.getName(), actor);
					final Class<? extends Actor> tempActor = PropsContext.remove(actor.getSimpleName());
					PropsContext.put(tempActor.getName(), tempActor);
				}
				else PropsContext.put(actor.getSimpleName(), actor);
			}
		}
		if(!PropsContext.isEmpty()) Logger.debug("Registering props: ");
		for(final String key : PropsContext.keySet()) {
			Logger.debug("Props for " + PropsContext.get(key).actorClass().getSimpleName() + " to name: " + key);
		}
	}

	private static ConfigurationBuilder build(String... namespaces) {
		final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		for(final String namespace : namespaces) {
			configBuilder.addUrls(ClasspathHelper.forPackage(namespace));
		}
		return configBuilder;
	}
	
	@SuppressWarnings("unchecked")
	private static void ScheduleActors(String... namespaces) {
		final ConfigurationBuilder configBuilder = build(namespaces);
		final Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));	
		final Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(Schedule.class);
		if(!schedules.isEmpty()) Logger.debug("Scheduling actors:");
		for(final Class<?> schedule : schedules) {
			final ActorRef actor = Akka.system().actorOf(GuiceProvider.get(Akka.system()).props((Class<? extends Actor>) schedule));
			final Schedule annotation = schedule.getAnnotation(Schedule.class);
			Akka.system().scheduler().schedule(
					Duration.apply(annotation.initialDelay(), annotation.timeUnit()),
					Duration.apply(annotation.interval(), annotation.timeUnit()),
					actor,
					"tick",
					Akka.system().dispatcher(),
					null);
			Logger.debug(schedule + " on delay: " + annotation.initialDelay() + " " + annotation.timeUnit() + " interval: " + annotation.interval() + " " + annotation.timeUnit());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void ScheduleOnceActors(String... namespaces) {
		final ConfigurationBuilder configBuilder = build(namespaces);
		final Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));		
		final Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(ScheduleOnce.class);
		if(!schedules.isEmpty()) Logger.debug("Scheduling actors once:");
		for(final Class<?> scheduleOnce : schedules) {
			final ActorRef actor = Akka.system().actorOf(GuiceProvider.get(Akka.system()).props((Class<? extends Actor>) scheduleOnce));
			final ScheduleOnce annotation = scheduleOnce.getAnnotation(ScheduleOnce.class);
			Akka.system().scheduler().scheduleOnce(
					Duration.apply(annotation.initialDelay(), annotation.timeUnit()),
					actor,
					"tick",
					Akka.system().dispatcher(),
					null);
			Logger.debug(scheduleOnce + " on delay: " + annotation.initialDelay() + " " + annotation.timeUnit());
		}
	}
}