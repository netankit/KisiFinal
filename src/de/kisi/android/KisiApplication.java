package de.kisi.android;

import android.app.Application;

public class KisiApplication extends Application{
	
	private static Application instance;
	public static Application getApplicationInstance(){
		return instance;
	}
	@Override
	public void onCreate(){
		super.onCreate();
		instance = this;
		
		// TODO: Uncomment this for release
		/*
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				 System.exit(1);
			}
		});
		*/
		
		de.kisi.android.api.KisiAPI.initialize(this);
		de.kisi.android.vicinity.manager.GeofenceManager.initialize(this);
		de.kisi.android.vicinity.LockInVicinityDisplayManager.initialize(this);
	}
}
