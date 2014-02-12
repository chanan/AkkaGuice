package services;
import play.Logger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akkaGuice.PropsContext;
import akkaGuice.annotations.RegisterActor;

import com.google.inject.Inject;

@RegisterActor
public class HelloActor extends UntypedActor {
	private final SayHello hello;
	
	@Inject
	public HelloActor(SayHello hello) {
		this.hello = hello;
	}

	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor: " + getSelf());
		hello.hello(getSelf().toString());
		
		//final ActorRef perRequestActorByName = getContext().actorOf(PropsContext.get("PerRequest"));
		//perRequestActorByName.tell("tick", getSelf());
		
		//final ActorRef perRequestActorByClass = getContext().actorOf(PropsContext.get(PerRequestActor.class));
		//perRequestActorByClass.tell("tick", getSelf());
	}
}