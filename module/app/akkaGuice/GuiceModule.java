package akkaGuice;
import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {
	private final String namespace;
	
	public GuiceModule(String namespace) {
		super();
		this.namespace = namespace;
	}
	
	protected void configure() {
		ActorScanner.ScanForActors(binder(), namespace);
	}
}
