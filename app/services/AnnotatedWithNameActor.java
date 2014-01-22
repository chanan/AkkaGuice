package services;
import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;

@RegisterActor("AnnotatedActor")
public class AnnotatedWithNameActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor using named annotation");
	}
}