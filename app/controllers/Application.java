package controllers;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import services.ServiceThatUsesActor;
import akka.actor.ActorRef;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Application extends Controller {
	private final ActorRef service;
	private final ServiceThatUsesActor serviceThatUsesActor;
	private final ActorRef annotatedActor;
	private final Html html = views.html.index.render();
	
	@Inject
	public Application(@Named("services.HelloActor") ActorRef service, 
			ServiceThatUsesActor serviceThatUsesActor,
			@Named("AnnotatedActor") ActorRef annotatedActor) {
		this.service = service;
		this.serviceThatUsesActor = serviceThatUsesActor;
		this.annotatedActor = annotatedActor;
	}
	
    public Result index() {
    	service.tell("tick", null);
    	serviceThatUsesActor.speak();
    	annotatedActor.tell("hi there", null);
        return ok(html);
    }
}
