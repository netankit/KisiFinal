package de.kisi.android.vicinity.actor;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

/**
 * The ConformToUnlockActor is a simple passthrough of the signal.
 * It sends a Message to the DisplayManager that a Vicinity of a Place has
 * been entered. The Lock should only open when the User confirm this signal.
 * 
 * @author Thomas Hörmann
 *
 */
public class ConfirmToUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry(int placeID, int lockId) {
		if(lockId != 0)
			LockInVicinityDisplayManager.getInstance().addLock(placeID,lockId);
		else
			LockInVicinityDisplayManager.getInstance().addPlace(placeID);
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		if(lockId != 0)
			LockInVicinityDisplayManager.getInstance().removeLock(placeID,lockId);
		else
			LockInVicinityDisplayManager.getInstance().removePlace(placeID);
	}

	@Override
	public void actOnEntry(Locator locator) {
		LockInVicinityDisplayManager.getInstance().addLock(locator.getPlaceId(),locator.getLockId());
	}

	@Override
	public void actOnExit(Locator locator) {
		LockInVicinityDisplayManager.getInstance().removeLock(locator.getPlaceId(),locator.getLockId());
	}

}
