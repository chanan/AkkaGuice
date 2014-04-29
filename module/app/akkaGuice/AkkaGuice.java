package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;
import play.Logger;
import play.libs.Akka;

import com.google.inject.Injector;

public class AkkaGuice {
	private AkkaGuice() { }

	public static void InitializeInjector(Injector injector, String... namespaces) {
		Logger.debug("Initialize Injector");
		GuiceProvider.get(Akka.system()).initialize(injector);
		ActorScanner.ScheduleActors(namespaces);
		ActorScanner.ScheduleOnceActors(namespaces);
	}
}