package de.kisi.android.vicinity.actor;

import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class CompositActor implements LockInVicinityActorInterface{

	private LockInVicinityActorInterface[] actorList;
	
	public CompositActor(LockInVicinityActorInterface[] actorList){
		this.actorList = actorList;
	}
	@Override
	public void actOnEntry(int placeID) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnEntry(placeID);
	}

	@Override
	public void actOnExit(int placeID) {
		for(LockInVicinityActorInterface actor:actorList)
			actor.actOnExit(placeID);
	}

}
