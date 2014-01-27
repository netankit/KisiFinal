package de.kisi.android.notifications;

import java.util.LinkedList;

public class NotificationPlace {
	int id;
	String name;
	LinkedList<NotificationLock> locks;
	public NotificationPlace(int id, String name){
		this.id=id;
		this.name = name;
		 locks = new LinkedList<NotificationLock>();
	}

}
