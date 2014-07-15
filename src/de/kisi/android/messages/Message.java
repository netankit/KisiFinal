package de.kisi.android.messages;

import java.io.Serializable;

/**
 * This class represents an push message, that is sent when you ring the bell of a public place.
 */
public class Message implements Serializable{
	private int sender; //Quickblox user id of sender
	private int receiver; //Kisi user id of receiver
	private long time; //when to ring button was pressed; in ms
	private String type; //ring
	private int place; //id of the place
	
	public Message(int sender, int receiver, String type, int place) {
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.place = place;
		this.time=System.currentTimeMillis();
	}
	
	public int getSender() {
		return sender;
	}
	public void setSender(int sender) {
		this.sender = sender;
	}
	public int getReceiver() {
		return receiver;
	}
	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getPlace() {
		return place;
	}
	public void setPlace(int place) {
		this.place = place;
	}
}
