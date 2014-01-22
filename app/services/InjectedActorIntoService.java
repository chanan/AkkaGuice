package services;
import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;

@RegisterActor
public class InjectedActorIntoService extends UntypedActor {
	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from InjectedActorIntoService that was injected into ServiceThatUsesActor");
	}
}