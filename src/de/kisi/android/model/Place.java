package de.kisi.android.model;

import java.util.ArrayList;
import java.util.List;

//import com.google.android.maps.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Place {
	private int id;
	private String name;
	private List<Lock> locks;
	private String updated_at;
	//private GeoPoint location;
	private double latitude, longitude;
	/*private String streetName, streetNumber;
	private String zip, city;
	private String state, country;
	private String additionalInformation; */
	private int owner_id;

	
	public Place(JSONObject json) {
		try {
			id 				= json.getInt("id");
			name 			= json.getString("name");
			locks 			= null;
			updated_at 		= json.getString("updated_at"); // "2013-06-26T15:53:42Z"
			/*location = new GeoPoint(
						(int)(json.getDouble("latitude") *1000000.0), 
						(int)(json.getDouble("longitude")*1000000.0))
			);*/
			try {
				latitude 		= json.getDouble("latitude");
				longitude 		= json.getDouble("longitude");
			}
			catch (JSONException e) {
				latitude = 0;
				longitude = 0;
			}
			
			//streetName 		= json.getString("street_name");
			//streetNumber	= json.getString("street_number");
			//zip 			= json.getString("zip");
			//city 			= json.getString("city");
			//country 		= json.getString("country");
			//state 			= json.getString("state");
			//additionalInformation = json.getString("additional_information");
			owner_id 		= json.getInt("user_id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public List<Lock> getLocks() {
		if ( locks == null ) {
			// TODO
		}
		return locks;
	}

	public String getUpdatedAt() {
		return updated_at;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	/*
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
	*/
	public int getOwnerId() {
		return owner_id;
	}

	public void setLocks(JSONArray data) {
		locks = new ArrayList<Lock>();
		try {
			for (int i=0; i<data.length(); i++) {
				Lock lock = new Lock(data.getJSONObject(i));
				locks.add(lock);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
