package akkaGuice;

import play.Application;
import play.Play;
import play.Plugin;

public class AkkaGuicePlugin extends Plugin {
    private final Application application;

    public AkkaGuicePlugin(Application application) {
        this.application = application;
    }

    private static AkkaGuicePlugin plugin() {
        return Play.application().plugin(AkkaGuicePlugin.class);
    }

    public static ClassLoader getClassLoader() {
        return plugin().application.classloader();
    }
}