import services.ServiceThatUsesActor;
import services.ServiceThatUsesActorImpl;

import com.google.inject.AbstractModule;

public class ServicesThatUseActorsModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(ServiceThatUsesActor.class).to(ServiceThatUsesActorImpl.class);
	}
}