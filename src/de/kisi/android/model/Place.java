package de.kisi.android.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.manager.GeofenceManager;




//TODO: tk: libraries like Lombok simplify with automating getter & setter creation
//@DatabaseTable
public class Place {
	@DatabaseField(id = true, index=true)
	private int id;
	@DatabaseField
	private String name;
	@ForeignCollectionField(eager=false)
    private ForeignCollection<Lock> locks;	
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

	
	public Place () {}
	
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public boolean areLocksLoaded(){
		return locksLoaded;
	}
	
	//TODO: clean this up 
	public List<Lock> getLocks() {
		Lock[] lockArray = locks.toArray(new Lock[0]);
		List<Lock> result = new ArrayList<Lock>();
		for(Lock l: lockArray) {
			result.add(l);
		}
		return result;
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

	
	public boolean getNotificationEnabled() {
		Context context = KisiApplication.getApplicationInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		return prefs.getBoolean(generateSharedPreferencesKey(), true);
	}
	
	public void setNotificationEnabled(boolean value) {
		Context context = KisiApplication.getApplicationInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(generateSharedPreferencesKey(), value);
		editor.commit();
		//remove  Geofence 
		if(value == false) {
			GeofenceManager.getInstance().removeGeofance(this); 
			NotificationManager.removeNotifications(KisiApplication.getApplicationInstance(), this);
			return;
		}
		//add Geofence
		if(value == true) {
			KisiAPI.getInstance().updatePlaces(new OnPlaceChangedListener() {

				@Override
				public void onPlaceChanged(Place[] newPlaces) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
	}
	
	private String generateSharedPreferencesKey() {
		if(KisiAPI.getInstance().getUser() != null) {
			return this.id + "-" + KisiAPI.getInstance().getUser().getId();
		}
		else {
			return "";
		}
	}

}
