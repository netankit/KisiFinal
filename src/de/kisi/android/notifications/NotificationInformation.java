package de.kisi.android.notifications;

import android.app.Notification;

class NotificationInformation {
	public enum Type{
		Place,
		Lock
	}
	public Notification notification;
	public Type type;
	public int notificationId;
	public int typeId;
}
