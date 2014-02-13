package akkaGuice;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import play.Logger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;

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
			if(isSingleton(actor)) {
				Logger.debug("Binding class " + actor.getSimpleName() + " to name: " + key + " Singleton Scoped.");
				binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toProvider(new ActorRefProvider(actor)).in(Singleton.class);
			} else {
				Logger.debug("Binding class " + actor.getSimpleName() + " to name: " + key + " Request Scoped.");
				binder.bind(ActorRef.class).annotatedWith(Names.named(key)).toProvider(new ActorRefProvider(actor));
				Logger.debug("Registering Props for class " + actor.getSimpleName() + " to name: " + key + " in PropsContext.");
				PropsContext.put(key, actor);
			}
		}
	}
	
	private static boolean isSingleton(Class<? extends UntypedActor> actor) {
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