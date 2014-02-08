package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import play.Configuration;
import play.Logger;
import play.Play;
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
	private static Configuration config = Play.application().configuration();
	
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
			Logger.debug("Binding class " + actor.getSimpleName() + " to name: " + key);
			binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toInstance(Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor), key));
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
			long initialDelay = 0;
			long interval = 0;
			TimeUnit timeUnitInitial = TimeUnit.MILLISECONDS;
			TimeUnit timeUnitInterval = TimeUnit.MILLISECONDS;
			String configInitial = schedule.getName() + ".initialDelay";
			String configInterval = schedule.getName() + ".interval";
			String configEnabled = schedule.getName() + ".enabled";
			if(config.getString(configEnabled) != null && !config.getBoolean(configEnabled)) continue;
			if(config.getString(configInitial) != null) {
				initialDelay = getTime(config.getString(configInitial));
				timeUnitInitial = getTimeUnit(config.getString(configInitial));
			} else {
				initialDelay = annotation.initialDelay();
				timeUnitInitial = annotation.timeUnit();
			}
			if(config.getString(configInterval) != null) {
				interval = getTime(config.getString(configInterval));
				timeUnitInterval = getTimeUnit(config.getString(configInterval));
			} else {
				interval = annotation.interval();
				timeUnitInterval = annotation.timeUnit();
			}
			Akka.system().scheduler().schedule(
					Duration.apply(initialDelay, timeUnitInterval),
					Duration.apply(interval, timeUnitInterval),
					actor,
					"tick",
					Akka.system().dispatcher(),
					null);
			Logger.debug(schedule + " on delay: " + initialDelay + " " + timeUnitInitial + " interval: " + interval + " " + timeUnitInterval);
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
			long initialDelay = 0;
			TimeUnit timeUnit = TimeUnit.MILLISECONDS;
			String configName = scheduleOnce.getName() + ".initialDelay";
			String configEnabled = scheduleOnce.getName() + ".enabled";
			if(config.getString(configEnabled) != null && !config.getBoolean(configEnabled)) continue;
			if(config.getString(configName) != null) {
				initialDelay = getTime(config.getString(configName));
				timeUnit = getTimeUnit(config.getString(configName));
			} else {
				initialDelay = annotation.initialDelay();
				timeUnit = annotation.timeUnit();
			}
			Akka.system().scheduler().scheduleOnce(
					Duration.apply(initialDelay, timeUnit),
					actor,
					"tick",
					Akka.system().dispatcher(),
					null);
			Logger.debug(scheduleOnce + " on delay: " + initialDelay + " " + timeUnit);
		}
	}
	
	private static TimeUnit getTimeUnit(String duration) {
		String trimmed = duration.trim().toLowerCase();
		if(trimmed.endsWith("ns") || trimmed.endsWith("nanosecond") || trimmed.endsWith("nanoseconds")) return TimeUnit.NANOSECONDS;
		if(trimmed.endsWith("us") || trimmed.endsWith("microsecond") || trimmed.endsWith("microseconds")) return TimeUnit.MICROSECONDS;
		if(trimmed.endsWith("ms") || trimmed.endsWith("millisecond") || trimmed.endsWith("milliseconds")) return TimeUnit.MILLISECONDS;
		if(trimmed.endsWith("s") || trimmed.endsWith("second") || trimmed.endsWith("seconds")) return TimeUnit.SECONDS;
		if(trimmed.endsWith("m") || trimmed.endsWith("minute") || trimmed.endsWith("minutes")) return TimeUnit.MINUTES;
		if(trimmed.endsWith("h") || trimmed.endsWith("hour") || trimmed.endsWith("hours")) return TimeUnit.HOURS;
		if(trimmed.endsWith("d") || trimmed.endsWith("day") || trimmed.endsWith("days")) return TimeUnit.DAYS;
		else return TimeUnit.MILLISECONDS;
	}
	
	//TODO: Typesafe config has a getDuration in newer version, use that when play is updated
	private static long getTime(String duration) {
		String trimmed = duration.trim();
		String number = "";
		for(char ch : trimmed.toCharArray()) {
			if(Character.isDigit(ch)) number = number + ch;
			else break;
		}
		return Long.parseLong(number);
	}
}