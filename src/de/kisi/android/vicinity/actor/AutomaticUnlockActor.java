package de.kisi.android.vicinity.actor;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class AutomaticUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry(int placeID, int lockId) {
		Place place = KisiAPI.getInstance().getPlaceById(placeID);
		for(Lock lock : place.getLocks())
			if(lock.getId()==lockId)
				KisiAPI.getInstance().unlock(lock, null);
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		
	}

	@Override
	public void actOnEntry(Locator locator) {
		KisiAPI.getInstance().unlock(locator.getLock(),null);
		
	}

	@Override
	public void actOnExit(Locator locator) {
		
	}

}