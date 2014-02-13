package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.google.inject.Provider;

class ActorRefProvider implements Provider<ActorRef> {
	private final Class<? extends UntypedActor> actor;
	
	public ActorRefProvider(Class<? extends UntypedActor> actor) {
		this.actor = actor;
	}
	
	@Override
	public ActorRef get() {
		return Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(actor));
	}
}