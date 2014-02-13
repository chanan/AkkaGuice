package services;
import com.google.inject.Singleton;

import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;

@RegisterActor @Singleton
public class InjectedActorIntoService extends UntypedActor {
	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from InjectedActorIntoService that was injected into ServiceThatUsesActor " + getSelf());
	}
}