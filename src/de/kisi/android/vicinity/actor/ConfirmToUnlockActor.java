package de.kisi.android.vicinity.actor;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

/**
 * The ConformToUnlockActor is a simple passthrough of the signal.
 * It sends a Message to the DisplayManager that a Vicinity of a Place has
 * been entered. The Lock should only open when the User confirm this signal.
 * 
 */
public class ConfirmToUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry(Locator locator) {
		if(locator.getLockId() != 0)
			LockInVicinityDisplayManager.getInstance().addLock(locator.getPlaceId(),locator.getLockId());
		else
			LockInVicinityDisplayManager.getInstance().addPlace(locator.getPlaceId());
	}

	@Override
	public void actOnExit(Locator locator) {
		if(locator.getLockId() != 0)
			LockInVicinityDisplayManager.getInstance().removeLock(locator.getPlaceId(),locator.getLockId());
		else
			LockInVicinityDisplayManager.getInstance().removePlace(locator.getPlaceId());
			
	}

}
