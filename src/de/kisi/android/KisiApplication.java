package de.kisi.android;

import android.app.Application;

public class KisiApplication extends Application{
	
	@Override
	public void onCreate(){
		super.onCreate();
		de.kisi.android.vicinity.LockInVicinityDisplayManager.instanciate(getApplicationContext());
	}
}
