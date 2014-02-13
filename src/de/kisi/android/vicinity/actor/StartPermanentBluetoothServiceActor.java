package de.kisi.android.vicinity.actor;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

/**
 * This Actor starts the BLE Service in foreground mode 
 */
public class StartPermanentBluetoothServiceActor implements LockInVicinityActorInterface{

	@Override
	public void actOnEntry(int placeID, int lockId) {
		// Start BLE and make sure it runs in foreground mode
		Place place = KisiAPI.getInstance().getPlaceById(placeID);
		for(Lock lock:place.getLocks())
			for(Locator locator:lock.getLocators())
				if(locator.getType().equals("BLE")){
					BluetoothLEManager.getInstance().startService(true);
					NotificationManager.getOrCreateBLEButtonNotification(KisiApplication.getApplicationInstance());
					return;
				}
		
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		// Stop the BLE Service, because it needs a lot of power
		// It is very unlikely that an iBeacon is outside of a Geofence, so 
		// we do not need to run the service in Background mode
		
		//BluetoothLEManager.getInstance().startService(false);
		NotificationManager.notifyBLEButtonNotificationDeleted();
		BluetoothLEManager.getInstance().stopService();
		
	}

	@Override
	public void actOnEntry(Locator locator) {
		// Start BLE and make sure it runs in foreground mode
		NotificationManager.getOrCreateBLEButtonNotification(KisiApplication.getApplicationInstance());
		BluetoothLEManager.getInstance().startService(true);
		
	}

	@Override
	public void actOnExit(Locator locator) {
		// Stop the BLE Service, because it needs a lot of power
		// It is very unlikely that an iBeacon is outside of a Geofence, so 
		// we do not need to run the service in Background mode
		NotificationManager.notifyBLEButtonNotificationDeleted();
		BluetoothLEManager.getInstance().stopService();//.startService(false);
		
	}

}
