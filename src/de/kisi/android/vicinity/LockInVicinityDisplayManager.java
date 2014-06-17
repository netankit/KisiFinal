package de.kisi.android.vicinity;

import de.kisi.android.KisiApplication;
import de.kisi.android.model.Locator;
import android.content.Context;
import android.content.Intent;

/**
 * This manager handles every content apart from the normal Application.
 * This contains Notifications and the Widgets so far. 
 */
public class LockInVicinityDisplayManager {

	// Transition Type Strings for the Broadcast Extra
	public static final String TRANSITION_ENTER_PLACE = "Enter Place"; 
	public static final String TRANSITION_EXIT_PLACE = "Exit Place"; 
	public static final String TRANSITION_ENTER_LOCK = "Enter Lock"; 
	public static final String TRANSITION_EXIT_LOCK = "Exit Lock";
	public static final String NO_TRANSITION = "Only Update"; 
	
	// instance for singleton access
	private static LockInVicinityDisplayManager instance;
	
	public static LockInVicinityDisplayManager getInstance(){
		if(instance == null)
			instance = new LockInVicinityDisplayManager(KisiApplication.getInstance());
		return instance;
	}
	
	// Context is needed to send broadcasts
	public Context mContext;
	
	private LockInVicinityDisplayManager(Context context){
		mContext = context;
	}
	
	/**
	 * A vicinity manager noticed that there is a lock near to the phone
	 * The place parameter is used for a faster traverse to find the lock
	 * A wrong placeId can cause that a valid lockId could not be found.
	 * 
	 * @param placeId Id of the Place where the lock belongs to
	 * @param lockId Id of the Lock
	 */
	public void addLock(Locator locator){
		int placeId = locator.getPlaceId();
		int lockId = locator.getLockId();
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Sender", locator.getType());
		intent.putExtra("Lock", lockId);
		intent.putExtra("Type", TRANSITION_ENTER_LOCK);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * A vicinity manager noticed the phone was moved apart from a lock
	 * that previously was claimed as "near".
	 * The place parameter is used for a faster traverse to find the lock
	 * A wrong placeId can cause that a valid lockId could not be found.
	 * 
	 * @param placeId Id of the Place where the lock belongs to
	 * @param lockId Id of the Lock
	 */
	public void removeLock(Locator locator){
		int placeId = locator.getPlaceId();
		int lockId = locator.getLockId();
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		intent.putExtra("Type", TRANSITION_EXIT_LOCK);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * There was no transition, but there have something changed that 
	 * might cause another state on displayed items.
	 * For example notifications are dis-/enabled in the settings.
	 */
	public void update(){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Type", NO_TRANSITION);
		mContext.sendBroadcast(intent);
	}

	/**
	 * A vicinity manager noticed that there is a place near to the phone
	 * 
	 * @param placeId Id of the Place
	 */
	public void addPlace(Locator locator){
		int placeId = locator.getPlaceId();
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Sender", locator.getType());
		intent.putExtra("Type", TRANSITION_ENTER_PLACE);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * A vicinity manager noticed the phone was moved apart from a place
	 * that previously was claimed as entered.
	 * 
	 * @param placeId Id of the Place
	 */
	public void removePlace(Locator locator){
		int placeId = locator.getPlaceId();
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Type", TRANSITION_EXIT_PLACE);
		mContext.sendBroadcast(intent);
	}
}
