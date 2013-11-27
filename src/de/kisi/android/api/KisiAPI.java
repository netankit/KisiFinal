package de.kisi.android.api;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.kisi.android.model.*;

// TODO: Create a real API
public class KisiAPI {

	private static KisiAPI instance = new KisiAPI();  
	
	private Place[] places;
	private List<OnPlaceChangedListener> registeredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	public static KisiAPI getInstance(){
		return instance;
	}

	private KisiAPI(){
	}
	
	
	/**
	 * Get all available Places for the User
	 * 
	 * @return Array of all Places the user has access to
	 */
	public Place[] getPlaces(){
		places = new Place[4];
		try {
			JSONObject jo = new JSONObject();
			jo.put("id",1);
			jo.put("name", "Interim A");
			jo.put("updated_at", "");
			jo.put("user_id", "0");
			jo.put("latitude", 48.26335);
			jo.put("longitude", 11.66973);
			places[0]= new Place(jo);
			
			jo = new JSONObject();
			jo.put("id",2);
			jo.put("name", "MW B");
			jo.put("updated_at", "");
			jo.put("user_id", "0");
			jo.put("latitude", 48.26574);
			jo.put("longitude", 11.67103);
			places[1]= new Place(jo);		

			jo = new JSONObject();
			jo.put("id",3);
			jo.put("name", "Hochbrück");
			jo.put("updated_at", "");
			jo.put("user_id", "0");
			jo.put("latitude", 48.24694);
			jo.put("longitude", 11.63062);
			places[2]= new Place(jo);		

			jo = new JSONObject();
			jo.put("id",4);
			jo.put("name", "MW A");
			jo.put("user_id", "0");
			jo.put("updated_at", "");
			jo.put("latitude", 48.26562);
			jo.put("longitude", 11.66956);
			places[3]= new Place(jo);		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return places;
	}
	
	/**
	 * Register for interest in any changes in the Places.
	 * The listener is fired when a Place was added or deleted
	 * or any change occur within the Place like a Lock has been added. 
	 * @param listener
	 */
	public void registerOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			registeredOnPlaceChangedListener.add(listener);
	}
	/**
	 * Unregister for the listener registered in registerOnPlaceChangedListener()
	 * @param listener
	 */
	public void unregisterOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			registeredOnPlaceChangedListener.remove(listener);
	}
}
