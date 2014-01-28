package de.kisi.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartOnBootReceiver extends BroadcastReceiver{

	// This is by purpose an empty BroadcastReceiver
	// It's function is to create and start the KisiApplication Object
	@Override
	public void onReceive(Context arg0, Intent arg1) {
	}
}
