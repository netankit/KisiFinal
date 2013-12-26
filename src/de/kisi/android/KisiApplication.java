package de.kisi.android;

import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;
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
		
		GeofenceManager.getInstance();
		BluetoothLEManager.getInstance();
	}
}
