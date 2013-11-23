package de.kisi.android.vicinity.actor;

import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

public class ConfirmToUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry() {
		LockInVicinityDisplayManager.getInstance().notifyOnEntry();
	}

	@Override
	public void actOnExit() {
		LockInVicinityDisplayManager.getInstance().notifyOnExit();
	}

}
