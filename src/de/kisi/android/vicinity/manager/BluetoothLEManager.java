package de.kisi.android.vicinity.manager;

import de.kisi.android.KisiApplication;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class BluetoothLEManager {

	/**
	 * This Thread is responsible for shutting down the
	 * BLE Service after a predefined period of time.
	 * This decision was made because BLE needs a lot 
	 * of power. 
	 */
	private class ShutDownThread extends Thread{
		public static final int maxRuntimeInMinutes = 10;
		public long startTime;
		public void run(){
			startTime = System.currentTimeMillis();
			while(true){
				try{
					long curTime = System.currentTimeMillis();
					long sleepTime = Math.min(60000,startTime + maxRuntimeInMinutes * 60000 - curTime);
					sleep(sleepTime);
					if(System.currentTimeMillis() >= startTime + maxRuntimeInMinutes * 60000)
						break;
				}catch(Exception e){ }
			}
			stopService();
		}
		/**
		 * Reset the Time for shutdown
		 */
		public void reset(){
			startTime = System.currentTimeMillis();
		}
	}
	private static BluetoothLEManager instance;
	public static BluetoothLEManager getInstance(){
		if(instance == null)
			instance = new BluetoothLEManager();
		return instance;
	}
	
	private Context context;
	private Intent bluetoothServiceIntent;
	private ShutDownThread shutDownThread;
	private BluetoothLEManager(){
		context = KisiApplication.getApplicationInstance();
		// BLE Feature is available since Android 4.3, do nothing for lower versions
		// Also BLE hardware is not in all Android 4.3 and higher phones available
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothServiceIntent = new Intent(context,BluetoothLEService.class);
		}
		
	}
	
	/**
	 * Starts the iBeacon Framework. This can also be used to 
	 * switch between foreground and background mode.
	 * 
	 * @param runInForegroundMode true for foreground mode and false for background mode
	 */
	public void startService(boolean runInForegroundMode){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothServiceIntent.putExtra("foreground", runInForegroundMode);
			context.startService(bluetoothServiceIntent);
		}
		resetShutDownTime();
	}

	/**
	 * Stops the iBeacon Framework
	 */
	public void stopService(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			context.stopService(bluetoothServiceIntent);
		}
	}
	public void resetShutDownTime(){
		if(shutDownThread != null && shutDownThread.isAlive())
			shutDownThread.reset();
		else{
			// create a new thread only when the service is started
			// isAlive()==false means service has been stopped so this is also covered
			shutDownThread = new ShutDownThread();
			shutDownThread.start();
		}
	}
}