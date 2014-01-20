package akkaGuice;

import static akkaGuice.GuiceExtension.GuiceProvider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akkaGuice.annotations.Named;
import akkaGuice.annotations.Schedule;
import akkaGuice.annotations.ScheduleOnce;

import com.google.inject.Binder;
import com.google.inject.name.Names;


class ActorScanner {
	
	public static void ScanForActors(Binder binder, String... namespaces) {
		Logger.debug("Actor Scanner Started...");
		RegisterActors(binder, namespaces);
		ScheduleActors(namespaces);
		ScheduleOnceActors(namespaces);
	}
	
	private static void RegisterActors(Binder binder, String... namespaces) {
		Map<String, Class<? extends UntypedActor>> map = new HashMap<>();
		
		ConfigurationBuilder configBuilder = build(namespaces);
		
		Reflections reflections = new Reflections(configBuilder.setScanners(new SubTypesScanner()));
	
		Set<Class<? extends UntypedActor>> actors = reflections.getSubTypesOf(UntypedActor.class);
	
		for(final Class<? extends UntypedActor> actor : actors) {
			String nameFromAnnotation = getNamedFromAnnotation(actor);
			if(nameFromAnnotation != null) {
				map.put(nameFromAnnotation, actor);
			} else {
				if(map.containsKey(actor.getSimpleName())){
					map.put(actor.getName(), actor);
					final Class<? extends UntypedActor> tempActor = map.remove(actor.getSimpleName());
					map.put(tempActor.getName(), tempActor);
				}
				else map.put(actor.getSimpleName(), actor);
			}
		}
		if(map.size() > 0) Logger.debug("Registering actors: ");
		for(final String key : map.keySet()) {
			final Class<? extends UntypedActor> actor = map.get(key);
			binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toInstance(Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor)));
			Logger.debug("class " + actor.getSimpleName() + " to name: " + key);
		}
	}

	private static String getNamedFromAnnotation(final Class<? extends UntypedActor> actor) {
		String value = null;
		Annotation[] annotations = actor.getAnnotations();
		for(final Annotation annotation : annotations) {
			if(annotation instanceof Named) {
				Named named = (Named) annotation;
				value = named.value();
				break;
			}
		}
		return value;
	}

	private static ConfigurationBuilder build(String... namespaces) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		for(String namespace : namespaces) {
			configBuilder.addUrls(ClasspathHelper.forPackage(namespace));
		}
		return configBuilder;
	}
	
	@SuppressWarnings("unchecked")
	private static void ScheduleActors(String... namespaces) {
		ConfigurationBuilder configBuilder = build(namespaces);
		Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));
		
		Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(Schedule.class);
		if(schedules.size() > 0) Logger.debug("Scheduling actors:");
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
		ConfigurationBuilder configBuilder = build(namespaces);
		Reflections reflections = new Reflections(configBuilder.setScanners(new TypeAnnotationsScanner()));
		
		Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(ScheduleOnce.class);
		if(schedules.size() > 0) Logger.debug("Scheduling actors once:");
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
