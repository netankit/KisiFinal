package de.kisi.android;

import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;
import android.app.Application;
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
		
		GeofenceManager.getInstance();
		BluetoothLEManager.getInstance();
		// TODO: Uncomment this for release
		/*
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				 System.exit(1);
			}
		});
		*/
		de.kisi.android.db.DataManager.initialize(this);
		de.kisi.android.account.KisiAccountManager.initialize(this);
		de.kisi.android.api.KisiAPI.initialize(this);
		de.kisi.android.api.KisiLocationManager.initialize(this);
		//TODO: Uncomment this for release with geofance
//		de.kisi.android.vicinity.manager.GeofenceManager.initialize(this);
//		de.kisi.android.vicinity.LockInVicinityDisplayManager.initialize(this);
	}
	
	@Override
	public void onTerminate () {
		super.onTerminate();
		Log.d("kisi", "KisiApplication.onTerminate");
		de.kisi.android.db.DataManager.getInstance().close();
	}
}
