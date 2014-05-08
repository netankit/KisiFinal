package de.kisi.android.api;

/**
 * This Listener is used to get feedback about the state of a lock.
 */
public interface UnlockCallback {
	public void onUnlockSuccess(String message);
	public void onUnlockFail(String message);
}
