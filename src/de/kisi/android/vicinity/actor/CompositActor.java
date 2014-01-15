package de.kisi.android.vicinity.actor;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class CompositActor implements LockInVicinityActorInterface{

	private LockInVicinityActorInterface[] actorList;
	
	public CompositActor(LockInVicinityActorInterface[] actorList){
		this.actorList = actorList;
	}
	@Override
	public void actOnEntry(int placeID, int lockId) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnEntry(placeID, lockId);
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnExit(placeID, lockId);
	}
	@Override
	public void actOnEntry(Locator locator) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnEntry(locator);
	}
	@Override
	public void actOnExit(Locator locator) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnExit(locator);
	}

}
