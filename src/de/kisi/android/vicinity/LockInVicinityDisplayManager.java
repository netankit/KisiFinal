package de.kisi.android.vicinity;

import android.content.Context;
import android.content.Intent;

public class LockInVicinityDisplayManager {

	private static LockInVicinityDisplayManager instance;
	public static LockInVicinityDisplayManager getInstance(){
		return instance;
	}
	public Context mContext;
	private LockInVicinityDisplayManager(Context context){
		mContext = context;
	}
	
	public static void initialize(Context context){
		instance = new LockInVicinityDisplayManager(context);
	}
	public void notifyOnEntry(int placeID){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeID);
		intent.putExtra("Type", "Enter");
		mContext.sendBroadcast(intent);
	}
	public void notifyOnExit(int placeID){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		intent.putExtra("Place", placeID);
		intent.putExtra("Type", "Exit");
		mContext.sendBroadcast(intent);
	}
}
