package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import play.libs.Akka;
import akka.actor.Actor;
import akka.actor.Props;

public class PropsContext {
	private static Map<String, ActorHolder> map = new HashMap<String, ActorHolder>();
	
	public static Props get(Class<? extends Actor> clazz) {
		return GuiceProvider.get(Akka.system()).props(clazz);
	}
	
	public static Props get(String name) {
		ActorHolder actorHolder = map.get(name);
		if(actorHolder.isSingleton()) {
			return GuiceProvider.get(Akka.system()).props(map.get(name).getActor());
		} else {
			return GuiceProvider.get(Akka.system()).props(map.get(name).getActor());
		}
	}
	
	//Do not resolve the Props at this point. The injector might change. Do it on the get methods above
	protected static void put(String key, Class<? extends Actor> actor, boolean isSingleton) {
		map.put(key, new ActorHolder(actor, isSingleton));
	}
	
	protected static void put(String key, ActorHolder actorHolder) {
		map.put(key, actorHolder);
	}
	
	protected static boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	protected static Class<? extends Actor> remove(String key) {
		return map.remove(key).getActor();
	}
	
	protected static Set<String> keySet() {
		return map.keySet();
	}

	protected static boolean isEmpty() {
		return map.isEmpty();
	}
}