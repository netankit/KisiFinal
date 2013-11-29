package de.kisi.android.api;

public interface UnlockCallback {
	public void onUnlockSuccess(String message);
	public void onUnlockFail(String message);
}
