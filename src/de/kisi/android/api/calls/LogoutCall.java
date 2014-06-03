package de.kisi.android.api.calls;

public class LogoutCall extends GenericCall {

	public LogoutCall() {
		super("/users/sign_out", HTTPMethod.DELETE);
	}

}
