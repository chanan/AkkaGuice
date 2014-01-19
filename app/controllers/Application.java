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
	private final Html html = views.html.index.render();
	
	@Inject
	public Application(@Named("services.HelloActor") ActorRef service, ServiceThatUsesActor serviceThatUsesActor) {
		this.service = service;
		this.serviceThatUsesActor = serviceThatUsesActor;
	}
	
    public Result index() {
    	service.tell("tick", null);
    	serviceThatUsesActor.speak();
        return ok(html);
    }
}
