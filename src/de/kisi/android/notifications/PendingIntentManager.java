package de.kisi.android.notifications;

import java.util.Hashtable;

import android.app.PendingIntent;
import android.content.Intent;
import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
// TODO: Use this Manager also for the Widgets
/**
 * This Manager manages the pending Intents,
 * there can be a lot pending Intents in the Application for
 * each Place or Lock. Since the extra bundle gets overwritten
 * for two PendingIntents with the same id
 */
public class PendingIntentManager {

	private static PendingIntentManager instance;
	// singleton instance
	public static PendingIntentManager getInstance(){
		if(instance == null)
			instance = new PendingIntentManager();
		return instance;
	}
	
	// index is used to make each PendingIntent id unique per place or lock
	private int index = 1;
	
	// use the same id for the same place or lock
	private Hashtable<Integer,Integer> usedPlaces = new Hashtable<Integer,Integer>();
	private Hashtable<Integer,Integer> usedLocks = new Hashtable<Integer,Integer>();
	
	//index for the bluetooth intent
	private int bluetoothIntent = -1;
	
	// private constructor for singleton instance
	private PendingIntentManager(){
	}
	
	public PendingIntent getPendingIntentForPlace(Integer placeId){
		if(!usedPlaces.containsKey(placeId))
			usedPlaces.put(placeId, index ++);
		return createPendingIntent(placeId,-1,usedPlaces.get(placeId));
	}
	
	public PendingIntent getPendingIntentForLock(Integer placeId, Integer lockId){
		if(!usedLocks.containsKey(lockId))
			usedLocks.put(lockId, index ++);
		return createPendingIntent(placeId,lockId,usedLocks.get(lockId));
	}
	
	public PendingIntent getPendingIntentForBluetooth() {
		if(bluetoothIntent == -1) {
			bluetoothIntent = index++;
		}
		Intent intent = new Intent(BluetoothLEManager.BLUETOOTH_INTENT);
		return PendingIntent.getBroadcast(KisiApplication.getInstance(), bluetoothIntent, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	
	private PendingIntent createPendingIntent(Integer placeId, Integer lockId, int pendingIntentId){
		Intent intent = new Intent(KisiApplication.getInstance(), KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		return PendingIntent.getActivity(KisiApplication.getInstance(), pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
