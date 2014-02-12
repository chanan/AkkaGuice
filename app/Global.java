import play.Application;
import play.GlobalSettings;
import akkaGuice.AkkaGuice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Global extends GlobalSettings {
	//private Injector injector = Guice.createInjector(new GuiceModule());
	private Injector injector;

	public <A> A getControllerInstance(Class<A> clazz) throws Exception {
		return injector.getInstance(clazz);
	}

	@Override
	public void onStart(Application arg0) {
		//injector = AkkaGuice.Startup(injector, "services");
		//injector = injector.createChildInjector(new ServicesThatUseActorsModule());
		injector = Guice.createInjector(new CombinedGuiceModule());
		AkkaGuice.InitializeInjector(injector);
	}
}