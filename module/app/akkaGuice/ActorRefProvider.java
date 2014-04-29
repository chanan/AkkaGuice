package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;
import play.libs.Akka;
import akka.actor.Actor;
import akka.actor.ActorRef;

import com.google.inject.Provider;

import java.util.Random;

class ActorRefProvider implements Provider<ActorRef> {
	private final Class<? extends Actor> actor;
    private final String key;
    private final Random rnd = new Random();
	
	public ActorRefProvider(Class<? extends Actor> actor, String key, boolean singleton) {
		this.actor = actor;
        if(singleton) this.key = key;
        else this.key = key + "-" + Math.abs(rnd.nextLong());
	}
	
	@Override
	public ActorRef get() {
		return Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor), key);
	}
}