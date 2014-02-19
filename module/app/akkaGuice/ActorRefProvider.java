package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;
import play.libs.Akka;
import akka.actor.Actor;
import akka.actor.ActorRef;

import com.google.inject.Provider;

class ActorRefProvider implements Provider<ActorRef> {
	private final Class<? extends Actor> actor;
	
	public ActorRefProvider(Class<? extends Actor> actor) {
		this.actor = actor;
	}
	
	@Override
	public ActorRef get() {
		return Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor));
	}
}