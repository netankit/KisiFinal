package de.kisi.android.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Event {
	
	private int id;
	@SerializedName("user_id")
	private int userId;
	private boolean failed;
	@SerializedName("created_at")
	private Date createdAt;
	@SerializedName("place_id")
	private int placeId;
	private String message;
	@SerializedName("user_agent")
	private String userAgent;
	private String address;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public boolean isFailed() {
		return failed;
	}
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public int getPlaceId() {
		return placeId;
	}
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
}
