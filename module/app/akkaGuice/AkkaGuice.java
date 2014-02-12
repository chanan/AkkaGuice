package akkaGuice;
import static akkaGuice.GuiceExtension.GuiceProvider;
import play.Logger;
import play.libs.Akka;

import com.google.inject.Binder;
import com.google.inject.Injector;

public class AkkaGuice {
	private AkkaGuice() { }

	public static Injector Startup(Injector injector, String namespace) {
		Logger.debug("Akka Guice Startup...");
		GuiceProvider.get(Akka.system()).initialize(injector);
		return injector.createChildInjector(new AkkaGuiceModule(namespace));
	}
	
	public static void ScanForActors(Binder binder, String... namespaces) {
		ActorScanner.ScanForActors(binder, namespaces);
	}
	
	public static void InitializeInjector(Injector injector) {
		Logger.debug("InitializeInjector");
		GuiceProvider.get(Akka.system()).initialize(injector);
	}
}