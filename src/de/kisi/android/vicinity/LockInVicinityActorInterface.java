package de.kisi.android.vicinity;

public interface LockInVicinityActorInterface {
	public void actOnEntry(int placeID, int lockId);
	public void actOnExit(int placeID, int lockId);
}
