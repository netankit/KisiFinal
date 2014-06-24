package de.kisi.android.notifications;

import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.notifications.NotificationInformation.Type;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

// TODO: When a new Lock is added after the Notification is shown refresh the Notification
/**
 * The Notification Manager is realized as a BroadcastReceiver. 
 * This decision was made, so that the Widget Manager can be handled in the same way.
 * For showing Notifications use the LockInVicinityDisplayManager.
 * The LockInVicinityDisplayManager handles the send of the different 
 * Broadcasts.
 */
public class NotificationManager extends BroadcastReceiver {

	private static Hashtable<Integer,String> enteredPlaces = new Hashtable<Integer,String>();
	private static Hashtable<Integer,String> enteredLocks = new Hashtable<Integer,String>();

	@Override
	public void onReceive(Context context, Intent intent) {
		// handle the received intent
		handleIntent(intent);
		
		// handle all changes
		updateNotifications();
	}
	
	private static Context getDefaultContext(){
		return KisiApplication.getInstance();
	}
	
	private void updateNotifications(){
		Context context = getDefaultContext();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Place[] places = KisiAPI.getInstance().getPlaces();
		// This is a test for removed notifications
		for(NotificationInformation info:notifications) {
			info.valid = false;
		}
		for(Place place:places){
			if(enteredPlaces.containsKey(place.getId()) && place.getNotificationEnabled()){
				if(!containsNotificationForPlace(place)){
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
						createNotificationForPlace(place, enteredPlaces.get(place.getId()));
					}else {
						createNotificationForPlaceSupport(place, enteredPlaces.get(place.getId()));
					}		
				} else {
					getNotificationForPlace(place).valid = true;
				}
			}
			for(Lock lock:place.getLocks()){
				if(enteredLocks.containsKey(lock.getId())) {
					if(!containsNotificationForLock(lock)) {
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
							createNotificationForLock(place, lock, enteredLocks.get(lock.getId()));
						}else {
							createNotificationForLockSupport(place, lock, enteredLocks.get(lock.getId()));
						}
					}else {
						getNotificationForLock(lock).valid = true;
					}
				}
			}
		}
		// remove invalid notifications
		LinkedList<NotificationInformation> removeList = new LinkedList<NotificationInformation>(); 
		for(NotificationInformation info:notifications) {
			if(!info.valid && !info.BLEButton){
				mNotificationManager.cancel(info.notificationId);
				removeList.add(info);
			}
		}
		for(NotificationInformation info:removeList)
			notifications.remove(info);

	}
	
	
	private static void createNotificationForPlace(Place place, String sender){
		NotificationInformation info = getBLENotification();
		Context context = getDefaultContext();
		if(info != null && info.type == NotificationInformation.Type.BLEOnly){
			info.notification = getExpandedNotification(place, info, context, sender);
			info.type = Type.Place;
			info.typeId = place.getId();
			info.object = place;
		}else{
			info = new NotificationInformation();
			info.notification = getExpandedNotification(place, sender);
			info.notificationId = getUnusedId();
			info.object = place;
			info.type = Type.Place;
			info.typeId = place.getId();
			notifications.add(info);
		}
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(info.notificationId, info.notification);
	}
	
	private static Notification getExpandedNotification(Place place, String sender){
		NotificationInformation info = new NotificationInformation();
		return getExpandedNotification(place, info, getDefaultContext(), sender); 
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static Notification getExpandedNotification(Place place, NotificationInformation info, Context context, String sender){
		NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
		nb.setSmallIcon(R.drawable.notification_icon);
		nb.setContentTitle("KISI");
	    nb.setContentText(place.getName());
	    nb.setDefaults(Notification.DEFAULT_ALL);
	    nb.setOnlyAlertOnce(true);
		nb.setWhen(0);
		nb.setOngoing(true);
		nb.setContentIntent(PendingIntentManager.getInstance().getPendingIntentForPlace(place.getId(), sender));
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
		contentView.setTextViewText(R.id.place, place.getName());

		if(info.BLEButton) {
			RemoteViews bluetoothButton =  new RemoteViews(context.getPackageName(), R.layout.notification_bluetooth_button);
			bluetoothButton.setOnClickPendingIntent(R.id.bluetoothButton, PendingIntentManager.getInstance().getPendingIntentForBluetooth());
			contentView.addView(R.id.bluetoothframelayout, bluetoothButton);
			contentView.setViewVisibility(R.id.bluetoothButton, View.VISIBLE);
			if(!info.containsBLE) {
				contentView.setImageViewResource(R.id.bluetoothButton, R.drawable.ic_action_bluetooth);	
				contentView.setTextViewText(R.id.bleInfo, getDefaultContext().getText(R.string.bluetooth_off));
			} else {
				contentView.setImageViewResource(R.id.bluetoothButton, R.drawable.ic_action_bluetooth_searching);
				contentView.setTextViewText(R.id.bleInfo, getDefaultContext().getText(R.string.bluetooth_on));
			}
		}
		else {
			contentView.setViewVisibility(R.id.bluetoothButton, View.GONE);
			contentView.setTextViewText(R.id.bleInfo, "");
		}
		
		contentView.removeAllViews(R.id.widget2);
		int buttonCount = 0;
		for(Lock lock : place.getLocks()){
			// do not display more than 3 lock button, because otherwise each button is too small
			// just show then three dots
			if(buttonCount >= 3) {
				RemoteViews textView;
				textView = new RemoteViews(context.getPackageName(), R.layout.notifcation_text);
				contentView.addView(R.id.widget2, textView);
				break;
			}
			RemoteViews button;
			button = new RemoteViews(context.getPackageName(), R.layout.notification_item);
			button.setTextViewText(R.id.unlockButton, lock.getName());
			button.setOnClickPendingIntent(R.id.unlockButton, PendingIntentManager.getInstance().getPendingIntentForLock(place.getId(), lock.getId(), sender));
			contentView.addView(R.id.widget2, button);
			buttonCount++;
		}
		Notification notification = nb.build();
		notification.bigContentView = contentView;
		notification.contentView = contentView;
		//keep the notification with the BLE button at the top by set its timestamp to the future 
		if(info.BLEButton) {
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.when = System.currentTimeMillis()+100000000;
		}
		
		return notification;
	}
	
	private static void createNotificationForLock(Place place, Lock lock, String sender){
		NotificationInformation info = new NotificationInformation();
		info.type = NotificationInformation.Type.Lock;
		info.typeId = lock.getId();
		info.object = lock;
		info.notificationId = getUnusedId();
		
		Context context = KisiApplication.getInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setLargeIcon(( (BitmapDrawable)getDefaultContext().getResources().getDrawable(R.drawable.ic_notification_icon)).getBitmap());
		nc.setContentText("Touch to Unlock");
		nc.setContentTitle(lock.getName() + " - " + place.getName());
		nc.setDefaults(Notification.DEFAULT_ALL);
		nc.setWhen(0);
		nc.setContentIntent(PendingIntentManager.getInstance().getPendingIntentForLock(place.getId(), lock.getId(), sender));

		info.notification = nc.build();
		notifications.add(info);
		mNotificationManager.notify(info.notificationId, info.notification);
	}


	
	private void handleIntent(Intent intent){
		Bundle extras = intent.getExtras();
		String type = extras.getString("Type");
		String sender = extras.getString("Sender");
		int lockId = extras.getInt("Lock", -1);
		int placeId = extras.getInt("Place", -2);
		if(type.equals(LockInVicinityDisplayManager.TRANSITION_ENTER_LOCK)){
			enteredLocks.put(lockId,sender);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_EXIT_LOCK)){
			enteredLocks.remove(lockId);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_ENTER_PLACE)){
			enteredPlaces.put(placeId,sender);
		}else if(type.equals(LockInVicinityDisplayManager.TRANSITION_EXIT_PLACE)){
			enteredPlaces.remove(placeId);
		}
	}

	
	private static LinkedList<NotificationInformation> notifications = new LinkedList<NotificationInformation>();

	public static void removeAllNotification() {
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getDefaultContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}
	
	
	
	public static NotificationInformation getOrCreateBLEServiceNotification(Context context, String sender){
		NotificationInformation bleNotification = getBLEButtonNotification();
		if(bleNotification != null){
			android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getDefaultContext().getSystemService(Context.NOTIFICATION_SERVICE);
			bleNotification.containsBLE = true;
			
			//TODO: implement solution for place with disabled notification
			if(bleNotification.type == Type.BLEOnly){		
				//bleNotification.notification = getNotificationForBLEButton();
			}
			else {
				bleNotification.notification = getExpandedNotification((Place) bleNotification.object, bleNotification, getDefaultContext(), sender);
				mNotificationManager.notify(bleNotification.notificationId, bleNotification.notification);
			}
			
		}
		return bleNotification; 
	}

	public static void notifyBLEServiceNotificationDeleted(String sender) {
		NotificationInformation bleNotification = getBLEButtonNotification();
		if(bleNotification != null){
			android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getDefaultContext().getSystemService(Context.NOTIFICATION_SERVICE);	
			bleNotification.containsBLE = false;
			
			//TODO: implement solution for place with disabled notification
			if(bleNotification.type == Type.BLEOnly){
				//	bleNotification.notification = getNotificationForBLEButton();
			}
			else {
				bleNotification.notification = getExpandedNotification((Place) bleNotification.object, bleNotification, getDefaultContext(), sender);
				mNotificationManager.notify(bleNotification.notificationId, bleNotification.notification);
			}
			LockInVicinityDisplayManager.getInstance().update();
		}
	}
	

	/**
	 * There is at most one notification that contains information
	 * about, that BLE is running. If this notification exists 
	 * this will be returned, otherwise it returns null
	 * @return Notification with BLE information, null if no such Notification exists
	 */
	public static NotificationInformation getBLENotification(){
		for(NotificationInformation info : notifications)
			if(info.containsBLE)
				return info;
		return null;
	}
	
	
	public static NotificationInformation getOrCreateBLEButtonNotification(Context context, Place place, String sender){
		NotificationInformation bleButtonNotification = getBLEButtonNotification();
		if(bleButtonNotification != null){
			// if BLE Button is already in a Notification Do nothing
			return bleButtonNotification;
		}else{
			bleButtonNotification = getNotificationForBLEButton(context, place);			
			bleButtonNotification.BLEButton = true;
			if(bleButtonNotification.type == NotificationInformation.Type.Place) {
				bleButtonNotification.notification = getExpandedNotification((Place)bleButtonNotification.object, bleButtonNotification, context, sender);				
			}
			android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getDefaultContext().getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(bleButtonNotification.notificationId, bleButtonNotification.notification);
			return bleButtonNotification;
		}
	}
	
	public static void notifyBLEButtonNotificationDeleted(String sender) {
		NotificationInformation bleButtonNotification = getBLEButtonNotification();
		if(bleButtonNotification != null){
			android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getDefaultContext().getSystemService(Context.NOTIFICATION_SERVICE);
			bleButtonNotification.BLEButton = false;
			if(bleButtonNotification.type == Type.BLEOnly && !bleButtonNotification.containsBLE){
				mNotificationManager.cancel(bleButtonNotification.notificationId);
			}else {
				bleButtonNotification.notification = getExpandedNotification((Place) bleButtonNotification.object, bleButtonNotification, getDefaultContext(), sender);
				mNotificationManager.notify(bleButtonNotification.notificationId, bleButtonNotification.notification);
			}
			LockInVicinityDisplayManager.getInstance().update();
		}
	}
	
	
	
	
	/**
	 * There is at most one notification that contains a button 
	 * to turn on/off BLE. If this notification exists 
	 * this will be returned, otherwise it returns null
	 * @return Notification with BLE Button information, null if no such Notification exists
	 */
	
	public static NotificationInformation getBLEButtonNotification() {
		for (NotificationInformation info : notifications)
			if (info.BLEButton)
				return info;
		return null;
	}
	
	/**
	 * Every place has its unique Notification, if there exists a
	 * Notification for this Place true will be returned, false otherwise 
	 * @param place Place
	 * @return true if there is a Notification for the Place, false otherwise
	 */
	public static boolean containsNotificationForPlace(Place place){
		for(NotificationInformation info : notifications)
			if(info.type == Type.Place)
				if(info.typeId == place.getId())
					return true;
		return false;
	}
	
	public static NotificationInformation getNotificationForPlace(Place place){
		for(NotificationInformation info : notifications)
			if(info.type == Type.Place)
				if(info.typeId == place.getId())
					return info;
		return null;
	}
	
	/**
	 * Every Lock has its unique Notification, if there exists a
	 * Notification for this Lock true will be returned, false otherwise 
	 * @param lock Lock
	 * @return true if there is a Notification for the Lock, false otherwise
	 */
	public static boolean containsNotificationForLock(Lock lock){
		for(NotificationInformation info : notifications)
			if(info.type == Type.Lock)
				if(info.typeId == lock.getId())
					return true;
		return false;
	}
	
	public static NotificationInformation getNotificationForLock(Lock lock){
		for(NotificationInformation info : notifications)
			if(info.type == Type.Lock)
				if(info.typeId == lock.getId())
					return info;
		return null;
	}
	
	/**
	 * Try to find a Notification that can show that BLE is 
	 * running. So far only Place Notifications can contain BLE 
	 * information.
	 *  
	 * @param context Context is needed for building the Notification
	 * @return Returns a valid Notification object that contains a Notification that can be used
	 */
	public static NotificationInformation getNotificationForBLE(Context context){
		for(NotificationInformation info : notifications){
			if(info.type == Type.Place)
				return info;
		}
		
		// At this point no carrier for BLE was found, create a new one	
		NotificationInformation info = new NotificationInformation();
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setLargeIcon(( (BitmapDrawable)getDefaultContext().getResources().getDrawable(R.drawable.ic_notification_icon)).getBitmap());
		nc.setContentText("KISI");
		nc.setContentTitle("BluetoothLE running");
		nc.setWhen(0);
		info.containsBLE = true;
		info.notificationId = getUnusedId();
		info.notification = nc.build();
		info.type = Type.BLEOnly;
		notifications.add(info);
		return info;
	}
	
	/**
	 * Try to find a Notification that can include a Button to turn
	 * on/off BLE. So far only Place Notifications can contain a BLE Button 
	 * information.
	 *  
	 * @param context Context is needed for building the Notification
	 * @return Returns a valid Notification object that contains a Notification that can be used
	 */
	public static NotificationInformation getNotificationForBLEButton(Context context, Place place){
		for(NotificationInformation info : notifications){
			if(info.type == Type.Place) {
				if(info.typeId == place.getId())
				return info;
			}
		}
		
		NotificationInformation info = new NotificationInformation();

		NotificationCompat.Builder nb = new NotificationCompat.Builder(context);	
		nb.setSmallIcon(R.drawable.notification_icon);
		nb.setLargeIcon(( (BitmapDrawable)getDefaultContext().getResources().getDrawable(R.drawable.ic_notification_icon)).getBitmap());
		nb.setContentTitle("KISI");
	    nb.setDefaults(Notification.DEFAULT_ALL);
	    nb.setOnlyAlertOnce(true);
		nb.setWhen(0);
		nb.setOngoing(true);

		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
		
		RemoteViews bluetoothButton =  new RemoteViews(context.getPackageName(), R.layout.notification_bluetooth_button);
		bluetoothButton.setOnClickPendingIntent(R.id.bluetoothButton, PendingIntentManager.getInstance().getPendingIntentForBluetooth());
		contentView.addView(R.id.old_notification, bluetoothButton);
	
		info.notification = nb.build();
		info.notification.bigContentView = contentView;
		info.notification.contentView =  contentView;
		info.type = Type.BLEOnly;
		notifications.add(info);
		return info;
	}
	
	public static AutoUnlockNotificationInfo getBLEAutoUnlockNotifiction(Lock lock) {
		
		AutoUnlockNotificationInfo info = new AutoUnlockNotificationInfo();
		info.type = NotificationInformation.Type.AutoUnlock;
		info.typeId = lock.getId();
		info.object = lock;
		info.notificationId = getUnusedId();
		info.unlocking = true;
		
		Context context = KisiApplication.getInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setLargeIcon(( (BitmapDrawable)getDefaultContext().getResources().getDrawable(R.drawable.ic_notification_icon)).getBitmap());
		nc.setContentTitle("Unlocking " + lock.getName() + " ...");
		nc.setDefaults(Notification.DEFAULT_ALL);
		nc.setWhen((new Date()).getTime());

		info.notification = nc.build();
		notifications.add(info);
		mNotificationManager.notify(info.notificationId, info.notification);
		return info;
	}
	
	public static void setBLEAutoUnlockNotifictionResult(AutoUnlockNotificationInfo info) {
		
		notifications.remove(info);
		Context context = KisiApplication.getInstance();
		android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		//remove unlocking notification
		mNotificationManager.cancel(info.notificationId);
		NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
		nc.setDefaults(Notification.DEFAULT_ALL);
		nc.setSmallIcon(R.drawable.notification_icon);
		nc.setLargeIcon(( (BitmapDrawable)getDefaultContext().getResources().getDrawable(R.drawable.ic_notification_icon)).getBitmap());
		nc.setWhen((new Date()).getTime());
		//unlock was successful
		if(info.success) {
			nc.setContentTitle("Unlocked  " + ((Lock) (info.object)).getName());
		}else {
			nc.setContentTitle("Unlocking " +  ((Lock) (info.object)).getName() + " failed");
			nc.setContentText(info.message);
		}
			
		info.notificationId = getUnusedId();
		Notification notification = nc.build();
		mNotificationManager.notify(info.notificationId, notification);
	}

	
	
	
	
	/**
	 * Returns a new unused NotificationId
	 * @return NotificationId
	 */
	private static int getUnusedId(){
		int max = 0;
		for(NotificationInformation info : notifications)
			max = Math.max(info.notificationId, max);
		return max+1;
	}
	
	
	
	//****************************************************
	//*********** Support Code for Android 4.0 ***********
	//****************************************************
	
	private static void createNotificationForPlaceSupport(Place place, String sender){
		for(Lock lock : place.getLocks())
			createNotificationForLockSupport(place, lock, sender);
	}
	
	private static void createNotificationForLockSupport(Place place, Lock lock, String sender){
		createNotificationForLock(place, lock, sender);

	}
	
}
