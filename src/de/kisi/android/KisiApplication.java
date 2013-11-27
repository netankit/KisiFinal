package de.kisi.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

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
		
		de.kisi.android.vicinity.manager.GeofenceManager.initialize(this);
		de.kisi.android.vicinity.LockInVicinityDisplayManager.instanciate(this);
	}
}
