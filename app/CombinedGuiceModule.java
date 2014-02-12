import services.SayHello;
import services.SayHelloImpl;
import services.ServiceThatUsesActor;
import services.ServiceThatUsesActorImpl;
import akkaGuice.AkkaGuice;

import com.google.inject.AbstractModule;

public class CombinedGuiceModule extends AbstractModule {
	@Override
	protected void configure() {
		AkkaGuice.ScanForActors(binder(), "services");
		bind(SayHello.class).to(SayHelloImpl.class);
		bind(ServiceThatUsesActor.class).to(ServiceThatUsesActorImpl.class);
	}
}