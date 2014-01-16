package services;

import static akkaGuice.GuiceExtension.GuiceProvider;
import play.Logger;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.google.inject.Inject;

public class HelloActor extends UntypedActor {
	private final SayHello hello;
	
	@Inject
	public HelloActor(SayHello hello) {
		this.hello = hello;
	}

	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor: " + getSelf());
		hello.hello(getSelf().toString());
		final ActorRef perRequestActor = Akka.system().actorOf(GuiceProvider.get(Akka.system()).props(PerRequestActor.class));
		perRequestActor.tell("tick", getSelf());
	}
}
