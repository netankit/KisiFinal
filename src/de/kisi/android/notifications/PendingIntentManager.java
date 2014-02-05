package de.kisi.android.notifications;

import java.util.Hashtable;

import android.app.PendingIntent;
import android.content.Intent;
import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
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
	
	// private constructor for singleton instance
	private PendingIntentManager(){
	}
	
	public PendingIntent getPendingIntentForPlace(Integer placeId){
		if(!usedPlaces.containsKey(placeId))
			usedPlaces.put(placeId, index ++);
		return createPendingIntent(placeId,-1,usedPlaces.get(placeId));
	}
	
	public PendingIntent getPendingIntentForLock(Integer placeId, Integer lockId){
		if(!usedLocks.containsKey(placeId))
			usedLocks.put(placeId, index ++);
		return createPendingIntent(placeId,lockId,usedLocks.get(placeId));
	}
	
	
	private PendingIntent createPendingIntent(Integer placeId, Integer lockId, int pendingIntentId){
		Intent intent = new Intent(KisiApplication.getApplicationInstance(), KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		return PendingIntent.getActivity(KisiApplication.getApplicationInstance(), pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}