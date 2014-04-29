package services;

import play.Logger;

public class SayHelloImpl implements SayHello {
	@Override
	public void hello(String id) {
		Logger.info("Hello from service. Being called from: " + id);
	}
}
