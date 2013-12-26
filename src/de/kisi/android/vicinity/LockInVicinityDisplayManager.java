package de.kisi.android.vicinity;

import de.kisi.android.KisiApplication;
import android.content.Context;
import android.content.Intent;

public class LockInVicinityDisplayManager {

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
