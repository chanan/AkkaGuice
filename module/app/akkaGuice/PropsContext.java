package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import play.libs.Akka;
import akka.actor.Actor;
import akka.actor.Props;

public class PropsContext {
	private static Map<String, Class<? extends Actor>> map = new HashMap<String, Class<? extends Actor>>();
	
	public static Props get(Class<? extends Actor> clazz) {
		return GuiceProvider.get(Akka.system()).props(clazz);
	}
	
	public static Props get(String name) {
		return GuiceProvider.get(Akka.system()).props(map.get(name));
	}
	
	//Do not resolve the Props at this point. The injector might change. Do it on the get methods above
	protected static void put(String key, Class<? extends Actor> value) {
		map.put(key, value);
	}
	
	protected static boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	protected static Class<? extends Actor> remove(String key) {
		return map.remove(key);
	}
	
	protected static Set<String> keySet() {
		return map.keySet();
	}

	protected static boolean isEmpty() {
		return map.isEmpty();
	}
}