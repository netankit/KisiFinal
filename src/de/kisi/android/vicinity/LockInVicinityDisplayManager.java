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
	
	public static void instanciate(Context context){
		instance = new LockInVicinityDisplayManager(context);
	}
	public void notifyOnEntry(){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		mContext.sendBroadcast(intent);
	}
	public void notifyOnExit(){
		Intent intent = new Intent("de.kisi.android.VICINITY_CHANGED");
		mContext.sendBroadcast(intent);
	}
}
