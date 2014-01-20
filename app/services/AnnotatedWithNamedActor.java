package services;

import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.Named;

@Named("AnnotatedActor")
public class AnnotatedWithNamedActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor using named annotation");
	}
}