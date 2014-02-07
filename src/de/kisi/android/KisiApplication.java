package de.kisi.android;

import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;
import android.app.Application;


public class KisiApplication extends Application {

	private static Application instance;
	/**
	 * Retuns a valid Context object
	 * @author Thomas Hoermann
	 */
	public static Application getApplicationInstance() {
		return instance;
	}

	/**
	 * This is the first method called by Android.
	 * Here we use all the initialization of the different 
	 * Manager that have to start right before everything else.
	 * @author Thomas Hoermann
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// This have to be the first call.
		// Every Manager who needs a Context object uses the method 
		// getApplicationInstance()
		instance = this;	
		// Start LocationManager for location Updates
		de.kisi.android.api.KisiLocationManager.getInstance();

		// Start GeofenceManager for registering all Geofences
		GeofenceManager.getInstance();
		// Bluetooth gets triggered by Geofences
		// So far the BLE Framework requires a lot of Power
		// Only run BLE when it is realy required
		//BluetoothLEManager.getInstance().startService(false);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		de.kisi.android.db.DataManager.getInstance().close();
	}

}
