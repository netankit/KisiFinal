package de.kisi.android.vicinity.actor;

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
	public void actOnEntry(int placeID) {
		LockInVicinityDisplayManager.getInstance().notifyOnEntry(placeID);
	}

	@Override
	public void actOnExit(int placeID) {
		LockInVicinityDisplayManager.getInstance().notifyOnExit(placeID);
	}

}
