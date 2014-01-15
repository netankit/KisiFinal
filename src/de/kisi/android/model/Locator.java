package de.kisi.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

public class Locator {
	@DatabaseField(id = true)
	private int id;
	
	@DatabaseField
	@SerializedName("place_id")
	private int placeId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=1)
	private Place place;
	
	@DatabaseField
	@SerializedName("lock_id")
	private int lockId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=1)
	private Lock lock;
	
	
	@DatabaseField
	private String name;
	
	@DatabaseField
	private String kind;
	
	@DatabaseField
	private boolean enabled;
	
	@DatabaseField
	@SerializedName("suggest_unlock_enabled")
	private boolean suggestUnlockEnabled;
	
	//TODO: whats the type of treshold!!!
	@DatabaseField
	@SerializedName("suggest_unlock_treshold")
	private double suggestUnlockTreshold;
	
	
	//TODO:disable cause there is a bug in the backend that autoUnlockEnabled is a double instead of a boolean
//	@DatabaseField
//	@SerializedName("auto_unlock_enabled")
//	private boolean autoUnlockEnabled;
	
	@DatabaseField
	@SerializedName("auto_unlock_treshold")
	private double autoUnlockTreshold;
	
	@DatabaseField
	private double latitude;
	
	@DatabaseField
	private double longitude;
	
	@DatabaseField
	@SerializedName("ble_identifier")
	private String bleIdentifier;
	
	@DatabaseField
	@SerializedName("nfc_identifier")
	private String nfcIdentifier;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlaceId() {
		return placeId;
	}

	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}

	public int getLockId() {
		return lockId;
	}

	public void setLockId(int lockId) {
		this.lockId = lockId;
	}
	
	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSuggestUnlockEnabled() {
		return suggestUnlockEnabled;
	}

	public void setSuggestUnlockEnabled(boolean suggestUnlockEnabled) {
		this.suggestUnlockEnabled = suggestUnlockEnabled;
	}

	
	//TODO:disable cause there is a bug in the backend that autoUnlockEnabled is a double instead of a boolean
//	public int getSuggestUnlockTreshold() {
//		return suggestUnlockTreshold;
//	}
//
//	public void setSuggestUnlockTreshold(int suggestUnlockTreshold) {
//		this.suggestUnlockTreshold = suggestUnlockTreshold;
//	}
//
//	public boolean isAutoUnlockEnabled() {
//		return autoUnlockEnabled;
//	}
//
//	public void setAutoUnlockEnabled(boolean autoUnlockEnabled) {
//		this.autoUnlockEnabled = autoUnlockEnabled;
//	}
//
//	public int getAutoUnlockTreshold() {
//		return autoUnlockTreshold;
//	}

	public void setAutoUnlockTreshold(int autoUnlockTreshold) {
		this.autoUnlockTreshold = autoUnlockTreshold;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getBleIdentifier() {
		return bleIdentifier;
	}

	public void setBleIdentifier(String bleIdentifier) {
		this.bleIdentifier = bleIdentifier;
	}

	public String getNfcIdentifier() {
		return nfcIdentifier;
	}

	public void setNfcIdentifier(String nfcIdentifier) {
		this.nfcIdentifier = nfcIdentifier;
	}

}
