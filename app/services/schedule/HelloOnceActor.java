package services.schedule;

import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.ScheduleOnce;

@ScheduleOnce()
public class HelloOnceActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Say hello once!");
		
	}

}
