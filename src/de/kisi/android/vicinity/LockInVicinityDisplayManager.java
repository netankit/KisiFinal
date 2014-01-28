package de.kisi.android.vicinity;

import de.kisi.android.KisiApplication;
import android.content.Context;
import android.content.Intent;

public class LockInVicinityDisplayManager {

	public static final String TRANSITION_ENTER_PLACE = "Enter Place"; 
	public static final String TRANSITION_EXIT_PLACE = "Exit Place"; 
	public static final String TRANSITION_ENTER_LOCK = "Enter Lock"; 
	public static final String TRANSITION_EXIT_LOCK = "Exit Lock";
	public static final String NO_TRANSITION = "Only Update"; 
	
	private static LockInVicinityDisplayManager instance;
	public static LockInVicinityDisplayManager getInstance(){
		if(instance == null)
			instance = new LockInVicinityDisplayManager(KisiApplication.getApplicationInstance());
		return instance;
	}
	public Context mContext;
	private LockInVicinityDisplayManager(Context context){
		mContext = context;
	}
	
	public void addLock(int placeId, int lockId){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		intent.putExtra("Type", TRANSITION_ENTER_LOCK);
		mContext.sendBroadcast(intent);
	}
	public void removeLock(int placeId, int lockId){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		intent.putExtra("Type", TRANSITION_EXIT_LOCK);
		mContext.sendBroadcast(intent);
	}
	public void update(){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Type", NO_TRANSITION);
		mContext.sendBroadcast(intent);
	}

	public void addPlace(int placeId){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Type", TRANSITION_ENTER_PLACE);
		mContext.sendBroadcast(intent);
	}
	public void removePlace(int placeId){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeId);
		intent.putExtra("Type", TRANSITION_EXIT_PLACE);
		mContext.sendBroadcast(intent);
	}
}
