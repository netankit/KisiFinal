package de.kisi.android.notifications;

import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class NotificationManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (KisiApplication.isLoggedIn()) {
			KisiAPI kisiAPI = KisiAPI.getInstance();

			Place[] places = kisiAPI.getPlaces();
			Place place = null;
			Log.i("NotificationManager", "onReceive(" + intent.getAction()
					+ ")");
			Bundle extras = intent.getExtras();

			int placeID = extras.getInt("Place");
			String type = extras.getString("Type");
			for (Place element : places) {
				if (placeID == element.getId())
					place = element;
			}

			if (type.equals("Enter")) {
				List<Lock> locks = place.getLocks();

				for (int i = 0; i < locks.size(); i++) {

					showNotification(context, locks.get(i), place);
				}

			} else if (type.equals("Exit")) {
				removeNotifications(context, place);

			}

		}
	}

	private void removeNotifications(Context context, Place place) {

		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		List<Lock> locks = place.getLocks();
		for (Lock lock : locks) {
			int id = (lock.getId());
			mNotificationManager.cancel("unlock", id);
		}

	}

	/**
	 * 
	 * 
	 * @param c
	 *            Context is needed to show Notifications
	 * @param msg
	 *            The Message that should be shown on the Notification
	 */
	public void showNotification(Context c, Lock lock, Place place) {

		NotificationCompat.Builder nc = new NotificationCompat.Builder(c);
		long[] pattern = { 0, 800 };
		nc.setVibrate(pattern);
		nc.setSmallIcon(R.drawable.ic_launcher);
		nc.setContentText("Touch to Unlock");
		nc.setContentTitle(lock.getName() + "," + place.getName());
		nc.setDefaults(Notification.DEFAULT_SOUND);
		nc.setLights(0xFF0000FF, 100, 450);
		nc.setWhen(0);
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) c
				.getSystemService(Activity.NOTIFICATION_SERVICE);

		Intent intent = new Intent(c, KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", place.getId());
		intent.putExtra("Lock", lock.getId());

		PendingIntent pIntent = PendingIntent.getActivity(c, lock.getId(),
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		nc.setContentIntent(pIntent);

		int id = (lock.getId());
		mNotificationManager.notify("unlock", id, nc.build());

	}

	public static void initialize(KisiApplication kisiApplication) {
		// TODO Auto-generated method stub

	}
}
