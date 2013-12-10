package de.kisi.android.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;




//TODO: tk: libraries like Lombok simplify with automating getter & setter creation
//TODO: tk: Google's gson is a more full-featured JSON handler org.json
//e.g., it automates object creation from JSON strings
@DatabaseTable(tableName = "Places")
public class Place {
	@DatabaseField(id = true)
	private int id;
	@DatabaseField
	private String name;
	
	private List<Lock> locks;
	private boolean locksLoaded;
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longitude;
	@SerializedName("street_name")
	private String streetName;
	@SerializedName("street_number")
	private String streetNumber;
	private String zip;
	private String city;
	private String state;
	private String country;
	private String additionalInformation; 
	@DatabaseField
	@SerializedName("user_id")
	private int ownerId;
//	@SerializedName("updated_at")
//	private Date updatedAt;

	
	public Place () {	
		locks = new ArrayList<Lock>();
	}
	
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public boolean areLocksLoaded(){
		return locksLoaded;
	}
	public List<Lock> getLocks() {
		return locks;
	}
	

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	
	public String getAddress() {
		return getStreetName() + ", " + getCity();
	}
	
	public String getFullAddress() {
		return getStreet() + "\n" + 
			getZip() + " " + getCity() + "\n" +
			getCountry();
	}
	
	public String getStreet() {
		return streetName + " " + streetNumber;
	}
	
	public String getStreetName() {
		return streetName;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public String getZip() {
		return zip;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getState() {
		return state;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public int getOwnerId() {
		return ownerId;
	}
	
	
	public void setLock(Lock[] data) {
		locksLoaded = true;
		for(int i=0; i < data.length; i++) {
			locks.add(data[i]);
		}
	}
	
	
	@Override
	public String toString(){
		return name+" "+id;
	}

}
