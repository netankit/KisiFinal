package de.kisi.android.api;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.manavo.rest.RestCache;
import com.manavo.rest.RestCallback;
import com.manavo.rest.RestErrorCallback;

import de.kisi.android.KisiApi;
import de.kisi.android.R;
import de.kisi.android.model.*;

// TODO: Create a real API
public class KisiAPI {

	private static KisiAPI instance;  
	
	private Place[] places = new Place[0];
	private List<OnPlaceChangedListener> registeredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> unregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> newregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	
	private Context context;
	
	public static void initialize(Context context){
		instance = new KisiAPI(context);
	}
	public static KisiAPI getInstance(){
		return instance;
	}

	private KisiAPI(Context context){
		this.context = context;
	}
	/*
	private void createFakePlaces(){
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
			jo.put("id",87);
			jo.put("name", "MW A");
			jo.put("user_id", "0");
			jo.put("updated_at", "");
			jo.put("latitude", 48.26562);
			jo.put("longitude", 11.66956);
			places[3]= new Place(jo);		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		notifyAllOnPlaceChangedListener();
	}
	*/
	
	public void login(String login, String password, final LoginCallback callback, Activity activity){
		
		KisiApi api = new KisiApi(activity);
		api.authorize(login, password);
		api.setLoadingMessage("Logging in...");
		
		
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONObject data = (JSONObject)obj;

				try {
					Editor editor = context.getSharedPreferences("Config", Context.MODE_PRIVATE).edit();
					editor.putString("authentication_token", data.getString("authentication_token"));
					editor.putInt("user_id", data.getInt("id"));
					String plan_id =  data.getString("ei_plan_id");
					//backend returns "null"
					if(plan_id != "null")
						editor.putString("ei_plan_id", plan_id);
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				callback.onLoginSuccess();
			}
		});
		api.setErrorCallback(new RestErrorCallback () {
			@Override
			public void error(String message) {
				JSONObject json = null;
				String errormessage = null;
				try {
					json = new JSONObject(message);
					errormessage = json.getString("error");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Toast.makeText(context, errormessage, Toast.LENGTH_LONG).show();
				callback.onLoginFail();
			}} );
		
		
		api.post("users/sign_in");
	}
	
	public void logout(Activity activity){
		KisiApi api = new KisiApi(activity);
		
		api.setLoadingMessage(R.string.logout_in_progress);
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor = settings.edit();
				editor.remove("authentication_token");
				editor.commit();
				
				Toast.makeText(context, R.string.logout_successful, Toast.LENGTH_LONG).show();
			}
		});
		api.delete("users/sign_out");

	}
	
	/**
	 * Get all available Places for the User
	 * 
	 * @return Array of all Places the user has access to
	 */
	public Place[] getPlaces(){
		return places;
	}
	
	public Place getPlaceAt(int index){
		if(places != null && index>=0 && index<places.length)
			return places[index];
		return null;
	}
	public Place getPlaceById(int num){
		for(Place p : places)
			if(p.getId() == num)
				return p;
		return null;
	}
	
	public void updatePlaces(Activity activity) {
		RestCache.clear(activity);

		KisiApi api = new KisiApi(activity);
		api.setCachePolicy(RestCache.CachePolicy.CACHE_THEN_NETWORK);
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONArray locations_json = (JSONArray) obj;

				Hashtable<Integer,Place> placesHash = new Hashtable<Integer,Place>();
				try {
					for (int i = 0; i < locations_json.length(); i++) {
						Place location = new Place(locations_json.getJSONObject(i));
						// The API returned some locations twice, so let's check if we
						// already have it or not also check if the place has a locks 
						// otherwise just don't show it
						// this doesnt work until the backend supports it !!!!
						//if ((places.indexOfKey(location.getId()) < 0) && (!locations_json.getJSONObject(i).isNull("locks") )) {
						if (!placesHash.containsKey(location.getId())){	
							placesHash.put(location.getId(), location);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				places = new Place[placesHash.values().size()];
				int i=0;
				for(Place p : placesHash.values())
					places[i++]=p;
				notifyAllOnPlaceChangedListener();
			}

		});
		api.get("places");

	}
	
	public void updateLocks(Activity activity, final Place place){
		KisiApi api = new KisiApi(activity);

		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONArray data = (JSONArray) obj;

				place.setLocks(data);
				notifyAllOnPlaceChangedListener();
			}

		});
		api.setCachePolicy(RestCache.CachePolicy.CACHE_THEN_NETWORK);
		api.setLoadingMessage(null);
		api.get("places/" + String.valueOf(place.getId()) + "/locks");
	}
	

	public boolean createNewKey(Place p, String email, List<Lock> locks, final Activity activity) {

		KisiApi api = new KisiApi(activity);

		JSONArray lock_ids = new JSONArray();
		for (Lock l : locks) {
			lock_ids.put(l.getId());
		}
		JSONObject key = new JSONObject();
		try {
			key.put("lock_ids", lock_ids);
			key.put("assignee_email", email);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		api.addParameter("key", (Object) key);

		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONObject data = (JSONObject) obj;
				try {
					Toast.makeText(
							activity,
							String.format(context.getResources().getString(R.string.share_success),
								data.getString("assignee_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		api.post("places/" + String.valueOf(p.getId()) + "/keys");
		return true;
	}
	/**
	 * Register for interest in any changes in the Places.
	 * The listener is fired when a Place was added or deleted
	 * or any change occur within the Place like a Lock has been added. 
	 * @param listener
	 */
	public void registerOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			newregisteredOnPlaceChangedListener.add(listener);
	}
	/**
	 * Unregister for the listener registered in registerOnPlaceChangedListener()
	 * @param listener
	 */
	public void unregisterOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			unregisteredOnPlaceChangedListener.add(listener);
	}
	
	/**
	 * Notifies all the registered OnPlaceChangedListener that some Data regarding
	 * the Places has changed.
	 */
	private void notifyAllOnPlaceChangedListener(){
		for(OnPlaceChangedListener listener : unregisteredOnPlaceChangedListener)
			registeredOnPlaceChangedListener.remove(listener);
		registeredOnPlaceChangedListener.addAll(newregisteredOnPlaceChangedListener);
		newregisteredOnPlaceChangedListener.clear();
		for(OnPlaceChangedListener listener : registeredOnPlaceChangedListener)
			listener.onPlaceChanged(places);
	}
	
	public boolean userIsOwner(Place place){
		return place.getOwnerId()==KisiApi.getUserId();
	}
	
	public void unlock(Lock lock, Activity activity, final UnlockCallback callback){
		KisiApi api = new KisiApi(activity);
		// TODO: Does the server really have to know where we are
		// we sent so far always only (0,0)
		try {
			JSONObject location = new JSONObject();
			location.put("latitude", 0.0);
			location.put("longitude", 0.0);
			api.addParameter("location", location);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONObject json = (JSONObject) obj;
				String message = null;
				if(json.has("notice")) {
					// change button design
					try {
						message = json.getString("notice");
					} catch (JSONException e) {
						e.printStackTrace();
					}	
					callback.onUnlockSuccess(message);
					return;
				}
			}	
		});
		api.setErrorCallback(new RestErrorCallback () {
			@Override
			public void error(String message) {
				//change RestApi to avoid json parsing here?
				JSONObject json = null;
				try {
					json = new JSONObject(message);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(json.has("alert")) {
					String alertMsg = null;
					try {
						alertMsg = json.getString("alert");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					callback.onUnlockFail(alertMsg);
					return;
				}

			}});
		api.setLoadingMessage(R.string.opening);
		api.post(String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId()));	}
}
