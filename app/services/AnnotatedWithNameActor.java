package services;
import javax.inject.Named;

import play.Logger;
import akka.actor.UntypedActor;

import com.google.inject.Singleton;

@Named("AnnotatedActor") @Singleton
public class AnnotatedWithNameActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from actor using named annotation " + getSelf());
	}
}