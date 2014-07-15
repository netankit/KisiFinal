package de.kisi.android;

import com.quickblox.module.chat.smack.SmackAndroid;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.QuickBloxApi;
import de.kisi.android.messages.PlayServicesHelper;
import de.kisi.android.vicinity.manager.GeofenceManager;


public class KisiApplication extends Application {

	
	private static KisiApplication instance;
	/**
	 * Retuns a valid Context object
	 */
	public static KisiApplication getInstance() {
		return instance;
	}
	
	public String getVersion() throws NameNotFoundException {
		String versionName = (this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		return versionName;
	}
	public int getVersionCode() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}
	
	/**
	 * This is the first method called by Android.
	 * Here we use all the initialization of the different 
	 * Manager that have to start right before everything else.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// This have to be the first call.
		// Every Manager who needs a Context object uses the method 
		// getApplicationInstance()
		instance = this;	
		// Start GeofenceManager for registering all Geofences
		GeofenceManager.getInstance();
		// Bluetooth gets triggered by Geofences
		// So far the BLE Framework requires a lot of Power
		// Only run BLE when it is realy required
		//BluetoothLEManager.getInstance().startService(false);
		QuickBloxApi.getInstance();
		
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		de.kisi.android.db.DataManager.getInstance().close();
	}

}
