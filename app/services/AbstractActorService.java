package services;
import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.inject.Singleton;
import play.Logger;

@Singleton
public class AbstractActorService extends AbstractActor {
    public AbstractActorService() {
        receive(
            ReceiveBuilder.matchAny(
                obj -> Logger.info("AbstractActor Service is online: " + self())
            ).build()
        );
    }
}