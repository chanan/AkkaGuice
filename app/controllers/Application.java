package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.ServiceThatUsesActor;
import akka.actor.ActorRef;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import views.html.index;

public class Application extends Controller {
	private final ActorRef service;
	private final ServiceThatUsesActor serviceThatUsesActor;
	private final ActorRef annotatedActor;
    private final ActorRef abstractActorService;
    private final ActorRef perRequest;
	
	@Inject
	public Application(@Named("services.HelloActor") ActorRef service, 
			ServiceThatUsesActor serviceThatUsesActor,
			@Named("AnnotatedActor") ActorRef annotatedActor,
            @Named("AbstractActorService") ActorRef abstractActorService,
            @Named("PerRequest") ActorRef perRequest) {
		this.service = service;
		this.serviceThatUsesActor = serviceThatUsesActor;
		this.annotatedActor = annotatedActor;
        this.abstractActorService = abstractActorService;
        this.perRequest = perRequest;
	}
	
    public Result index() {
    	service.tell("tick", ActorRef.noSender());
    	serviceThatUsesActor.speak();
    	annotatedActor.tell("hi there", ActorRef.noSender());
        abstractActorService.tell("test", ActorRef.noSender());
        perRequest.tell("test", ActorRef.noSender());
        return ok(index.render());
    }
}
