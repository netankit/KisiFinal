package de.kisi.android.api;
/**
 * This callback is used to get notified whether the login
 * procedure was successful or not. 
 */
public interface LoginCallback {
	public void onLoginSuccess(String authtoken);
	public void onLoginFail(String errormessage);
}
