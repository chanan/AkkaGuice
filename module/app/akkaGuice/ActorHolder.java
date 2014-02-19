package akkaGuice;
import akka.actor.Actor;

class ActorHolder {
	private final Class<? extends Actor> actor;
	private final boolean  isSingleton;
	
	Class<? extends Actor> getActor() {
		return actor;
	}
	
	boolean isSingleton() {
		return isSingleton;
	}
	
	public ActorHolder(Class<? extends Actor> actor, boolean isSingleton) {
		this.actor = actor;
		this.isSingleton = isSingleton;
	}

	@Override
	public String toString() {
		return "ActorHolder [actor=" + actor + ", isSingleton=" + isSingleton + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actor == null) ? 0 : actor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ActorHolder other = (ActorHolder) obj;
		if (actor == null) {
			if (other.actor != null) return false;
		} else if (!actor.equals(other.actor)) return false;
		return true;
	}
}