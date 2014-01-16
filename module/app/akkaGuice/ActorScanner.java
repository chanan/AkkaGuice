package akkaGuice;

import static akkaGuice.GuiceExtension.GuiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.google.inject.Binder;
import com.google.inject.name.Names;


class ActorScanner {
	
	public static void ScanForActors(Binder binder, String namespace) {
		RegisterActors(binder, namespace);
		ScheduleActors(namespace);
		ScheduleOnceActors(namespace);
	}
	
	private static void RegisterActors(Binder binder, String namespace) {
		Map<String, Class<? extends UntypedActor>> map = new HashMap<>();
		
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(namespace))
        .setScanners(new SubTypesScanner()));
	
		Set<Class<? extends UntypedActor>> actors = reflections.getSubTypesOf(UntypedActor.class);
	
		for(final Class<? extends UntypedActor> actor : actors) {
			if(map.containsKey(actor.getSimpleName())){
				map.put(actor.getName(), actor);
				final Class<? extends UntypedActor> tempActor = map.remove(actor.getSimpleName());
				map.put(tempActor.getName(), tempActor);
			}
			else map.put(actor.getSimpleName(), actor);
		}
		for(final String key : map.keySet()) {
			final Class<? extends UntypedActor> actor = map.get(key);
			binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toInstance(Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor)));
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void ScheduleActors(String namespace) {
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(namespace))
		.setScanners(new TypeAnnotationsScanner()));
		
		Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(Schedule.class);
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
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void ScheduleOnceActors(String namespace) {
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(namespace))
		.setScanners(new TypeAnnotationsScanner()));
		
		Set<Class<?>> schedules = reflections.getTypesAnnotatedWith(ScheduleOnce.class);
		for(final Class<?> scheduleOnce : schedules) {
			final ActorRef actor = Akka.system().actorOf(GuiceProvider.get(Akka.system()).props((Class<? extends Actor>) scheduleOnce));
			final ScheduleOnce annotation = scheduleOnce.getAnnotation(ScheduleOnce.class);
			Akka.system().scheduler().scheduleOnce(
					Duration.apply(annotation.initialDelay(), annotation.timeUnit()),
					actor,
					"tick",
					Akka.system().dispatcher(),
					null);
		}
		
	}
}
