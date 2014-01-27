package de.kisi.android.notifications;

import java.util.LinkedList;

import android.app.Notification;

public class NotificationPlace {
	int id;
	String name;
	LinkedList<NotificationLock> locks;
	Notification notification;
	
	public NotificationPlace(int id, String name){
		this.id=id;
		this.name = name;
		locks = new LinkedList<NotificationLock>();
		notification = null;
	}

	public int getId(){
		return id;
	}
	public Notification getNotification(){
		return notification;
	}
}
