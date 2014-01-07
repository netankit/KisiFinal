package de.kisi.android.api;

public interface LoginCallback {
	public void onLoginSuccess(String authtoken);
	public void onLoginFail(String errormessage);
}
