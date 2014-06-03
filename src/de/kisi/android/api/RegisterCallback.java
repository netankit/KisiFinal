package de.kisi.android.api;
/**
* This callback is used to get notified whether the registration
* procedure was successful or not.
*/
public interface RegisterCallback {
public void onRegisterSuccess();
public void onRegisterFail(String errormessage);
}