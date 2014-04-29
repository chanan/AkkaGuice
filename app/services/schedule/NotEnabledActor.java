package services.schedule;

import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.Schedule;

@Schedule
public class NotEnabledActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("This should not print");
	}
}