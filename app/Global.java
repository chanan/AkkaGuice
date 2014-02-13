import play.Application;
import play.GlobalSettings;
import akkaGuice.AkkaGuice;
import akkaGuice.AkkaGuiceModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Global extends GlobalSettings {
	private Injector injector = Guice.createInjector(new AkkaGuiceModule("services"), new GuiceModule());

	public <A> A getControllerInstance(Class<A> clazz) throws Exception {
		return injector.getInstance(clazz);
	}

	@Override
	public void onStart(Application arg0) {
		AkkaGuice.InitializeInjector(injector, "services");
	}
}