package de.kisi.android.notifications;

import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import de.kisi.android.KisiMain;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class NotificationManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		KisiAPI kisiAPI = KisiAPI.getInstance();
		Log.i("BLE","Notification "+intent.getAction());
		if (kisiAPI.getUser() != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN){
				Bundle extras = intent.getExtras();
				Log.i("BLE","Notification "+extras.getString("Type")+extras.getInt("Place", -1));
				
				int placeId = extras.getInt("Place", -1);
				Log.i("BLE","Place has "+KisiAPI.getInstance().getPlaceById(placeId).getLocks().size()+" locks");
				String type = extras.getString("Type");
				if (type.equals("Enter")) 
					addPlace(KisiAPI.getInstance().getPlaceById(placeId));
				else
					removePlace(KisiAPI.getInstance().getPlaceById(placeId),context);
				showNotifications(context);
				
			}else{
				// For Android 4.0
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
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showNotifications(Context context){
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
			if(places.size()==0){
				mNotificationManager.cancelAll();
				return;
			}else{
				int i = 0;
				for(NotificationPlace place : places){
					createNotification(place,context);
					mNotificationManager.notify(++i, place.notification);

				}

		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static void createNotification(NotificationPlace place,Context context){

		NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
		nb.setSmallIcon(R.drawable.notification_icon)
		.setContentTitle("KISI")
	    .setContentText(place.name)
	    .setDefaults(Notification.DEFAULT_ALL)
		.setWhen(0);
		Intent intent = new Intent(context, KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", place.id);
		intent.putExtra("Lock", -1);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		nb.setContentIntent(pIntent);
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
		contentView.setTextViewText(R.id.place, place.name);
		Log.i("BLE","add place "+place.id);
		for(NotificationLock lock : place.locks){
			RemoteViews button;
			button = new RemoteViews(context.getPackageName(), R.layout.notification_item);
			button.setTextViewText(R.id.unlockButton, lock.name);
			intent = new Intent(context, KisiMain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("Type", "unlock");
			intent.putExtra("Place", place.id);
			intent.putExtra("Lock", lock.id);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, lock.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			button.setOnClickPendingIntent(R.id.unlockButton, pendingIntent);
			contentView.addView(R.id.widget2, button);
			Log.i("BLE","add lock "+lock.id);
		}
		Notification notification = nb.build();
		notification.bigContentView = contentView;
		place.notification = notification;
	}
	
	public static NotificationPlace setBLEServiceForeground(Context context){
		Log.i("BLE","Notification places.size()="+places.size());
		if(places.size()>0){
			NotificationPlace place = places.getFirst();
			createNotification(place,context);
			return place;
		}
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setSmallIcon(R.drawable.ic_launcher);
		nc.setContentText("Bluetooth running");
		nc.setContentTitle("KISI");
		NotificationPlace nPlace = new NotificationPlace(1," ");
		nPlace.notification = nc.build();
		return nPlace;
	}
	private static LinkedList<NotificationPlace> places = new LinkedList<NotificationPlace>();

	private static void addPlace(Place place){
		if(place == null)
			return;
		for(NotificationPlace p:places){
			if(p.id == place.getId()){
				places.remove(p);
				break;
			}
		}
		NotificationPlace newPlace = new NotificationPlace(place.getId(),place.getName());
		places.add(newPlace);
		for(Lock lock : place.getLocks()){
			Log.i("BLE","place"+place.getId()+" -> "+lock.getId());
			newPlace.locks.add(new NotificationLock(lock.getId(),lock.getName()));
		}
	}
	
	private static void removePlace(Place place, Context context){
		if(place == null)
			return;
		for(NotificationPlace p:places){
			if(p.id == place.getId()){
				android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(p.id);
				places.remove(p);
				return;
			}
		}
	}
	
	
	
	public static void removeNotifications(Context context, Place place) {

		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		List<Lock> locks = place.getLocks();
		for (Lock lock : locks) {
			int id = (lock.getId());
			mNotificationManager.cancel("unlock", id);
		}

	}


	public void showNotification(Context c, Lock lock, Place place) {

		NotificationCompat.Builder nc = new NotificationCompat.Builder(c);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setContentText("Touch to Unlock");
		nc.setContentTitle(lock.getName() + " - " + place.getName());
		nc.setDefaults(Notification.DEFAULT_ALL);
		nc.setWhen(0);
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
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
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}
}
