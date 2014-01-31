package de.kisi.android.notifications;

import android.app.Notification;

/**
 * Helperclass for the Notification Manager
 * This class stores information about the Notification
 * and raw data of the content within the Notification 
 */
public class NotificationInformation {
	public enum Type{
		Place,
		Lock,
		BLEOnly
	}
	public Notification notification;
	public Type type;
	public int notificationId;
	public int typeId;
	public Object object;
	public boolean containsBLE = false;
	public boolean valid = true;
	
	// Only allow the NotificatioManager to create instances
	protected NotificationInformation(){
	}
}
