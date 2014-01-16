package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import akka.actor.ActorRef;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Application extends Controller {
	private final ActorRef service;
	
	@Inject
	public Application(@Named("services.HelloActor") ActorRef service) {
		this.service = service;
	}
	
    public Result index() {
    	service.tell("tick", null);
        return ok(index.render("Your new application is ready."));
    }
}
