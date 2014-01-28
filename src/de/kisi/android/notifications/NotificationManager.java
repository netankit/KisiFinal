package de.kisi.android.notifications;

import java.util.HashSet;
import java.util.LinkedList;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;
import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

public class NotificationManager extends BroadcastReceiver {

	private static OnPlaceChangedListener listener;
	private static HashSet<Integer> enteredPlaces = new HashSet<Integer>();
	private static HashSet<Integer> enteredLocks = new HashSet<Integer>();
	private static LinkedList<NotificationInformation> shownNotifications = new LinkedList<NotificationInformation>(); 
	private static Boolean bleServiceRunning = false;
	private static int bleServiceNotificationId = 0;
	private static Boolean bleServiceNotificationSet = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		KisiAPI kisiAPI = KisiAPI.getInstance();
		if(listener == null){
			listener = new OnPlaceChangedListener(){

				@Override
				public void onPlaceChanged(Place[] newPlaces) {
					updateNotifications(newPlaces);
					
				}
			};
			kisiAPI.registerOnPlaceChangedListener(listener);
		}
		Log.i("BLE","Notification "+intent.getAction());
		Log.i("BLE","User "+kisiAPI.getUser());
		if (kisiAPI.getUser() != null) {
			handleIntent(intent);
			updateNotifications(kisiAPI.getPlaces());
		}
	}
	
	private void updateNotifications(Place[] places){
		Context context = KisiApplication.getApplicationInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		LinkedList<NotificationInformation> oldNotifications = shownNotifications;
		shownNotifications = new LinkedList<NotificationInformation>();
		for(Integer i:enteredPlaces){
			Log.i("BLE","enteredPlaces: "+i);
		}
		for(Place place:places){
			Log.i("BLE","Check Place "+place.getId()+ "contains: "+enteredPlaces.contains(place.getId())+" enabled: "+place.getNotificationEnabled());
			
			if(enteredPlaces.contains(place.getId()) && place.getNotificationEnabled()){
				createNotificationForPlace(place);
			}
		}
		if(shownNotifications.size()==0 && bleServiceRunning){
			
		}
		for(NotificationInformation oldInfo : oldNotifications){
			boolean found = false;
			for(NotificationInformation newInfo : shownNotifications){
				if(oldInfo.notificationId == newInfo.notificationId){
					found = true;
					break;
				}
			}
			if(!found){
				mNotificationManager.cancel(oldInfo.notificationId);
			}
		}
	}
	
	private static void createNotificationForPlace(Place place){
		Log.i("BLE","create for placd "+place.getId());
		Context context = KisiApplication.getApplicationInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			NotificationInformation info = new NotificationInformation();
			shownNotifications.add(info);	
			info.type = NotificationInformation.Type.Place;
			info.typeId = place.getId();
			info.object = place;
			if(bleServiceNotificationSet){
				info.notificationId = place.getId();
				info.notification = getExpandedNotification(place);
			}else{
				info.notificationId = bleServiceNotificationId;
				info.notification = getExpandedNotification(place, true, context);
			}
			mNotificationManager.notify(info.notificationId, info.notification);

		}else{// For Android 4.0
			for(Lock lock:place.getLocks())
				createNotificationForLock(place, lock);
		}
	}

	private static Notification getExpandedNotification(Place place){
		Context context = KisiApplication.getApplicationInstance();
		return getExpandedNotification(place, false, context); 
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static Notification getExpandedNotification(Place place, boolean isForBLE, Context context){
		NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
		nb.setSmallIcon(R.drawable.notification_icon);
		nb.setContentTitle("KISI");
	    nb.setContentText(place.getName());
	    nb.setDefaults(Notification.DEFAULT_ALL);
	    nb.setOnlyAlertOnce(true);
		nb.setWhen(0);
		nb.setOngoing(true);
		//nb.setContentIntent(getPendingIntent(place.getId(),0));
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
		contentView.setTextViewText(R.id.place, place.getName());
		if(isForBLE)
			contentView.setTextViewText(R.id.bleInfo, "Bluetooth LE running");
			
		contentView.removeAllViews(R.id.widget2);
		for(Lock lock : place.getLocks()){
			RemoteViews button;
			button = new RemoteViews(context.getPackageName(), R.layout.notification_item);
			button.setTextViewText(R.id.unlockButton, lock.getName());
			button.setOnClickFillInIntent(R.id.unlockButton, getIntent(place.getId(),lock.getId()));
			//button.setOnClickPendingIntent(R.id.unlockButton, getPendingIntent(place.getId(),lock.getId(),lock.getId()));
			contentView.addView(R.id.widget2, button);
		}
		Notification notification = nb.build();
		notification.bigContentView = contentView;
		notification.contentView = contentView;
		return notification;
	}
	
	private static void createNotificationForLock(Place place, Lock lock){
		NotificationInformation info = new NotificationInformation();
		shownNotifications.add(info);	
		info.type = NotificationInformation.Type.Lock;
		info.typeId = lock.getId();
		info.object = lock;
		info.notificationId = lock.getId();

		Context context = KisiApplication.getApplicationInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setContentText("Touch to Unlock");
		nc.setContentTitle(lock.getName() + " - " + place.getName());
		nc.setDefaults(Notification.DEFAULT_ALL);
	    nc.setOnlyAlertOnce(true);
		nc.setWhen(0);
		nc.setContentIntent(getPendingIntent(place.getId(),lock.getId()));

		info.notification = nc.build();
		mNotificationManager.notify(info.notificationId, info.notification);

	}

	private static PendingIntent getPendingIntent(int placeId, int lockId){
		Intent intent = new Intent(KisiApplication.getApplicationInstance(), KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		return PendingIntent.getActivity(KisiApplication.getApplicationInstance(), lockId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	private static Intent getIntent(int placeId, int lockId){
		Intent intent = new Intent(KisiApplication.getApplicationInstance(), KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Place", placeId);
		intent.putExtra("Lock", lockId);
		return intent;
	}
	
	private void handleIntent(Intent intent){
		Bundle extras = intent.getExtras();
		String type = extras.getString("Type");
		Log.i("BLE","Notification.Type = "+type);
		int lockId = extras.getInt("Lock", -1);
		int placeId = extras.getInt("Place", -2);
		if(type.equals(LockInVicinityDisplayManager.TRANSITION_ENTER_LOCK)){
			enteredLocks.add(lockId);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_EXIT_LOCK)){
			enteredLocks.remove(lockId);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_ENTER_PLACE)){
			Log.i("BLE","check Intent place = "+placeId);
			enteredPlaces.add(placeId);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_EXIT_PLACE)){
			enteredPlaces.remove(placeId);
		}
	}

	public static Pair<Integer,Notification> getBLEServiceNotification(Context context){
		if(!bleServiceRunning){
			bleServiceRunning = true;
			if(shownNotifications.size()>0){
				NotificationInformation info = shownNotifications.get(0);
				bleServiceNotificationId = info.notificationId;
				info.notification = getExpandedNotification((Place)info.object,true,context);
				return new Pair<Integer,Notification>(info.notificationId,info.notification);
			}else{
				bleServiceNotificationSet = false;
				bleServiceNotificationId = 1;
				NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
				nc.setSmallIcon(R.drawable.notification_icon);
				nc.setContentText("KISI");
				nc.setContentTitle("BluetoothLE running");
				nc.setDefaults(Notification.DEFAULT_ALL);
			    nc.setOnlyAlertOnce(true);
				nc.setWhen(0);
				return new Pair<Integer,Notification>(bleServiceNotificationId,nc.build());
			}
		}
		return null;
	}

	public static void notifyBLEServiceNotificationDeleted() {
		if(bleServiceRunning){
			bleServiceRunning = false;
			for(NotificationInformation info:shownNotifications){
				if(bleServiceNotificationId == info.notificationId){
					Context context = KisiApplication.getApplicationInstance();
					android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					info.notification = getExpandedNotification((Place)info.object);
					mNotificationManager.notify(info.notificationId, info.notification);
					break;
				}
			}
		}
	}

}
