package akkaGuice;
import com.google.inject.AbstractModule;

public class AkkaGuiceModule extends AbstractModule {
	private final String[] namespaces;
	
	public AkkaGuiceModule(String... namespaces) {
		super();
		this.namespaces = namespaces;
	}
	
	protected void configure() {
		ActorScanner.ScanForActors(binder(), namespaces);
	}
}