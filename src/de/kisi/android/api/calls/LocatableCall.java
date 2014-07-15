package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import de.kisi.android.vicinity.manager.GeofenceManager;

/**
 * This class is the generalization of a call, 
 * that sends the position of the device to the server.
 */
public abstract class LocatableCall extends GenericCall {

	protected LocatableCall(String path, HTTPMethod method) {
		super(path, method);
	}
	
	/**
	 * Overwrite this method if you want the location data 
	 * at a special position in the Json object
	 */
	@Override
	protected void createJson(){
		super.createJson();
		try {
			this.json.put("location", generateJSONLocation());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @return Returns the devices current position in a Json object.
	 */
	private JSONObject generateJSONLocation() {
		JSONObject location = new JSONObject();
		Location currentLocation = GeofenceManager.getInstance().getLocation();
		try {
    		if(currentLocation != null) {
    			location.put("latitude", currentLocation.getLatitude());
    			location.put("longitude", currentLocation.getLongitude());
    			location.put("horizontal_accuracy", currentLocation.getAccuracy());
    			location.put("altitude", currentLocation.getAltitude());
    			location.put("age", (System.currentTimeMillis() - currentLocation.getTime())/1000.0);
    		} else { 
	 			location.put("error:", "Location data not accessible");
    		}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        return location;
	}
}