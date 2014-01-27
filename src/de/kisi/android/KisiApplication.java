package de.kisi.android;

import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class KisiApplication extends Application {

	private static Application instance;
	
	public static Application getApplicationInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		GeofenceManager.getInstance();
		BluetoothLEManager.getInstance().startService(false);
		de.kisi.android.db.DataManager.initialize(this);;
		de.kisi.android.account.KisiAccountManager.initialize(this);
		de.kisi.android.api.KisiAPI.getInstance();
		de.kisi.android.api.KisiLocationManager.initialize(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d("kisi", "KisiApplication.onTerminate");
		de.kisi.android.db.DataManager.getInstance().close();
	}

}
