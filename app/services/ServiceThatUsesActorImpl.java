package services;
import akka.actor.ActorRef;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ServiceThatUsesActorImpl implements ServiceThatUsesActor {
	private final ActorRef actor;
	
	@Inject
	public ServiceThatUsesActorImpl(@Named("InjectedActorIntoService") ActorRef actor) {
		this.actor = actor;
	}
	
	@Override
	public void speak() {
		actor.tell("speak", null);
	}
}