package akkaGuice;

import static akkaGuice.GuiceExtension.GuiceProvider;
import play.Logger;
import play.libs.Akka;

import com.google.inject.Injector;

public class AkkaGuice {

	public static Injector Startup(Injector injector, String namespace) {
		Logger.debug("Akka Guice Startup...");
		GuiceProvider.get(Akka.system()).initialize(injector);
		return injector.createChildInjector(new GuiceModule(namespace));
	}
}
