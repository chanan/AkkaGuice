import services.SayHello;
import services.SayHelloImpl;
import services.ServiceThatUsesActor;
import services.ServiceThatUsesActorImpl;

import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SayHello.class).to(SayHelloImpl.class);
		bind(ServiceThatUsesActor.class).to(ServiceThatUsesActorImpl.class);
	}
}
