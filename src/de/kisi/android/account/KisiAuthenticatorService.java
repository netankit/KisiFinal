package de.kisi.android.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KisiAuthenticatorService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		
		KisiAuthenticator authenticator = new KisiAuthenticator(this);
        return authenticator.getIBinder();
	}

}
