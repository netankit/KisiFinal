package de.kisi.android.notifications;

import java.util.List;

import android.app.Activity;
import android.app.Notification; 
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import de.kisi.android.KisiMain;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class NotificationManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		KisiAPI kisiAPI = KisiAPI.getInstance();
		if (kisiAPI.getUser() != null) {
		
			Log.i("NotificationManager", "onReceive(" + intent.getAction() + ")");
			Bundle extras = intent.getExtras();
			
			int placeId = extras.getInt("Place", -1);
			String type = extras.getString("Type");
			
			Place place = kisiAPI.getPlaceById(placeId);
			
			if (type.equals("Enter")) {
				List<Lock> locks = place.getLocks();

				for(Lock l: locks){
					showNotification(context, l, place);
				}

			} else if (type.equals("Exit")) {
				removeNotifications(context, place);
			}

		}
	}

	public static void removeNotifications(Context context, Place place) {

		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		List<Lock> locks = place.getLocks();
		for (Lock lock : locks) {
			int id = (lock.getId());
			mNotificationManager.cancel("unlock", id);
		}

	}


	public void showNotification(Context c, Lock lock, Place place) {

		NotificationCompat.Builder nc = new NotificationCompat.Builder(c);
		nc.setSmallIcon(R.drawable.notification_icon);
		Bitmap bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.notification_icon);
		nc.setLargeIcon(bitmap);
		nc.setContentText("Touch to Unlock");
		nc.setContentTitle(lock.getName() + " - " + place.getName());
		nc.setDefaults(Notification.DEFAULT_ALL);
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);
		Intent intent = new Intent(c, KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", place.getId());
		intent.putExtra("Lock", lock.getId());

		PendingIntent pIntent = PendingIntent.getActivity(c, lock.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		nc.setContentIntent(pIntent);

		int id = lock.getId();
		mNotificationManager.notify("unlock", id, nc.build());

	}
	
	public static void removeAllNotifications(Context context) {
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}
}
