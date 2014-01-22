package services.schedule;
import java.util.concurrent.TimeUnit;

import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.RegisterActor;
import akkaGuice.annotations.Schedule;

@RegisterActor
@Schedule(initialDelay = 1, timeUnit = TimeUnit.SECONDS, interval = 2)
public class HelloActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.info("Hello from schedule package every 2 seconds");
	}
}