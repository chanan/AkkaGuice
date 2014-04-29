package services.schedule;
import play.Logger;
import akka.actor.UntypedActor;
import akkaGuice.annotations.Schedule;

@Schedule
public class HelloRepeatConfigActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		Logger.debug("Hello from Repeat actor from config");
	}
}