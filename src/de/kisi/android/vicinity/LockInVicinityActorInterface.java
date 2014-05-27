package de.kisi.android.vicinity;

import de.kisi.android.model.Locator;

/**
 * The interface that every Actor has to implement
 * 
 */
public interface LockInVicinityActorInterface {
	// Act for this locator
	public void actOnEntry(Locator locator);
	public void actOnExit(Locator locator);
}
