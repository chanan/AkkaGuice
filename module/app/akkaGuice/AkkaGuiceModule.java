package akkaGuice;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import play.Logger;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class AkkaGuiceModule extends AbstractModule {
	private final String[] namespaces;
	
	public AkkaGuiceModule(String... namespaces) {
		super();
		this.namespaces = namespaces;
	}
	
	protected void configure() {
		RegisterActors(binder(), namespaces);
	}
	
	private static void RegisterActors(Binder binder, String... namespaces) {
		Logger.debug("Actor Scanner Started...");
		final Map<String, ActorHolder> map = new HashMap<>();		
		final ConfigurationBuilder configBuilder = build(namespaces);
		final Reflections reflections = new Reflections(configBuilder.setScanners(new SubTypesScanner()));
		final Set<Class<? extends UntypedActor>> actors = reflections.getSubTypesOf(UntypedActor.class);
		for(final Class<? extends Actor> actor : actors) {
			final String named = getNamed(actor);
			final boolean isSingleton = isSingleton(actor);
			final ActorHolder actorHolder = new ActorHolder(actor, isSingleton);
			if(named != null) {
				map.put(named, actorHolder);
			} else {
				if(map.containsKey(actor.getSimpleName())){
					map.put(actor.getName(), actorHolder);
					final ActorHolder tempHolder = map.remove(actor.getSimpleName());
					map.put(tempHolder.getActor().getName(), tempHolder);
				}
				else map.put(actor.getSimpleName(), actorHolder);
			}
		}
		if(!map.isEmpty()) Logger.debug("Registering actors: ");
		for(final String key : map.keySet()) {
			final ActorHolder actorHolder = map.get(key);
			final Class<? extends Actor> actor = actorHolder.getActor();
			if(actorHolder.isSingleton()) {
				Logger.debug("Binding class " + actor.getSimpleName() + " to name: " + key + " Singleton Scoped.");
				binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toProvider(new ActorRefProvider(actor, key, true)).in(Singleton.class);
			} else {
				Logger.debug("Binding class " + actor.getSimpleName() + " to name: " + key + " Request Scoped.");
				binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toProvider(new ActorRefProvider(actor, key, false));
				PropsContext.put(key, actorHolder);
			}
		}
	}
	
	private static String getNamed(Class<? extends Actor> actor) {
		if(actor.getAnnotation(Named.class) == null) return null;
		Named named = actor.getAnnotation(Named.class);
		return named.value();
	}

	private static boolean isSingleton(Class<? extends Actor> actor) {
		return actor.getAnnotation(Singleton.class) != null;
	}

	private static ConfigurationBuilder build(String... namespaces) {
		final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		for(final String namespace : namespaces) {
			configBuilder.addUrls(ClasspathHelper.forPackage(namespace));
		}
		return configBuilder;
	}
}