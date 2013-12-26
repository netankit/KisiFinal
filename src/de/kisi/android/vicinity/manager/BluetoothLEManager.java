package de.kisi.android.vicinity.manager;

import de.kisi.android.KisiApplication;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Intent intent = new Intent(context,BluetoothLEService.class);
			context.bindService(intent, new ServiceConnection(){

				@Override
				public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				}

				@Override
				public void onServiceDisconnected(ComponentName arg0) {
				}}, Context.BIND_AUTO_CREATE);
		}
	}
	
	
}
