package de.kisi.android.notifications;

import java.util.GregorianCalendar;
import java.util.Random;

import de.kisi.android.R;


import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("NotificationManager","onReceive("+intent.getAction()+")");
		Bundle extras = intent.getExtras();
		showNotification(context, "Place "+extras.getInt("Place")+" "+extras.getString("Type"));
	}

	
	/**
	 * Just a simple Notification for testing purposes
	 * @param c Context is needed to show Notifications
	 * @param msg The Message that should be shown on the Notification
	 */
	public void showNotification(Context c, String msg){
		Random r = new Random();
		NotificationCompat.Builder nc = new NotificationCompat.Builder(c);
		nc.setSmallIcon(R.drawable.ic_launcher);
		GregorianCalendar cal = new GregorianCalendar();
		String text = ""+cal.get(GregorianCalendar.HOUR_OF_DAY)+":"+cal.get(GregorianCalendar.MINUTE)+":"+cal.get(GregorianCalendar.SECOND);
		nc.setContentText(text);
		nc.setContentTitle(msg);
		nc.setDefaults(Notification.DEFAULT_SOUND);
		nc.setLights(0xFF0000FF, 100, 450);
		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
		mNotificationManager.notify(r.nextInt(), nc.build());
	}
}
