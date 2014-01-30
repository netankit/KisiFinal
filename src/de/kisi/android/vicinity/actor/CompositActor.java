package de.kisi.android.vicinity.actor;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

/**
 * This Actor can combine different other Actors.
 * Its purpose is that other Actors can be single purpose actors only.
 * 
 */
public class CompositActor implements LockInVicinityActorInterface{

	private LockInVicinityActorInterface[] actorList;
	
	public CompositActor(LockInVicinityActorInterface[] actorList){
		this.actorList = actorList;
	}

	@Override
	public void actOnEntry(int placeID, int lockId) {
		for(LockInVicinityActorInterface actor:actorList)
			if(actor != null)
				actor.actOnEntry(placeID, lockId);
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		for(LockInVicinityActorInterface actor:actorList)
			if(actor != null)
				actor.actOnExit(placeID, lockId);
	}
	
	@Override
	public void actOnEntry(Locator locator) {
		for(LockInVicinityActorInterface actor:actorList)
			if(actor != null)
				actor.actOnEntry(locator);
	}
	
	@Override
	public void actOnExit(Locator locator) {
		for(LockInVicinityActorInterface actor:actorList)
			if(actor != null)
				actor.actOnExit(locator);
	}

}
