package de.kisi.android;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;
import de.kisi.android.vicinity.manager.GeofenceManager;


public class KisiApplication extends Application {

	private static KisiApplication instance;
	/**
	 * Retuns a valid Context object
	 * @author Thomas Hoermann
	 */
	public static KisiApplication getInstance() {
		return instance;
	}
	
	public String getVersion() throws NameNotFoundException {
		String versionName = (this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		return versionName;
	}
	
	public void showLoginScreen() {
		Intent login = new Intent(KisiApplication.getInstance(), KisiMain.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(login);
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
