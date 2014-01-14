package de.kisi.android.vicinity.manager;

import de.kisi.android.KisiApplication;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;

public class BluetoothLEManager {

	private static BluetoothLEManager instance;
	public static BluetoothLEManager getInstance(){
		if(instance == null)
			instance = new BluetoothLEManager();
		return instance;
	}
	
	private Context context;
	
	private BluetoothLEManager(){
		context = KisiApplication.getApplicationInstance();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Intent intent = new Intent(context,BluetoothLEService.class);
			context.startService(intent);
		}
	}
}
