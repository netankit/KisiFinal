package de.kisi.android.vicinity;

import de.kisi.android.model.Locator;

public interface LockInVicinityActorInterface {
	public void actOnEntry(int placeID, int lockId);
	public void actOnExit(int placeID, int lockId);
	public void actOnEntry(Locator locator);
	public void actOnExit(Locator locator);
}
