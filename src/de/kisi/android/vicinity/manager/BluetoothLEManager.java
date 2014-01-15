package de.kisi.android.vicinity.manager;

import de.kisi.android.KisiApplication;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class BluetoothLEManager {

	private static BluetoothLEManager instance;
	public static BluetoothLEManager getInstance(){
		if(instance == null)
			instance = new BluetoothLEManager();
		return instance;
	}
	
	private Context context;
	private Intent bluetoothServiceIntent;
	
	private BluetoothLEManager(){
		context = KisiApplication.getApplicationInstance();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothServiceIntent = new Intent(context,BluetoothLEService.class);
		}
	}
	
	public void startService(boolean runInForegroundMode){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothServiceIntent.putExtra("foreground", runInForegroundMode);
			context.startService(bluetoothServiceIntent);
		}
	}

	public void stopService(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			context.stopService(bluetoothServiceIntent);
		}
	}
}
