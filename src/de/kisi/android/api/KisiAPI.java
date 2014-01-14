package de.kisi.android.api;


import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;
import de.kisi.android.rest.KisiRestClient;

import com.google.gson.Gson;


public class KisiAPI {

	private static KisiAPI instance;  
	
	
	private Place[] places = new Place[0];
	private List<OnPlaceChangedListener> registeredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> unregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> newregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	
	private User user;

	private Context context;
	
	
	public static KisiAPI getInstance(){
		if(instance==null)
			instance = new KisiAPI(KisiApplication.getApplicationInstance());
		return instance;
	}

	private KisiAPI(Context context){
		this.context = context;
	}
	
	
	public void login(String login, String password, final LoginCallback callback){
		//cleaning the auth token before getting a new one 
		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.remove("authentication_token");
		editor.commit();
		
		JSONObject login_data = new JSONObject();
		JSONObject login_user = new JSONObject();
		try {
			login_data.put("email", login);
			login_data.put("password", password);
			login_user.put("user", login_data);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		KisiRestClient.getInstance().post(context, "users/sign_in", login_user,  new JsonHttpResponseHandler() {
			
			 public void onSuccess(org.json.JSONObject response) {
				String authtoken = null; 
				 try {
					Editor editor = context.getSharedPreferences("Config", Context.MODE_PRIVATE).edit();
					authtoken = response.getString("authentication_token");
					editor.putString("authentication_token", authtoken);
					
					Gson gson = new Gson();
					user = gson.fromJson(response.toString(), User.class);
					
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
				callback.onLoginSuccess(authtoken);
				return;
			}
			
			 public void onFailure(int statusCode, Throwable e, JSONObject response) {
				 String errormessage = null;
				 //no network connectivity
				 if(statusCode == 0) {
					 errormessage = context.getResources().getString(R.string.no_network);
					 callback.onLoginFail(errormessage);
					 return;
				 }
	
				 if(response != null) {
					try {
						errormessage = response.getString("error");
					} catch (JSONException ej) {
						ej.printStackTrace();
					}
				}
				else {
					errormessage = "Error!";
				}
				callback.onLoginFail(errormessage);
				return;
			};
			
		});
		
		
		

	}
	
	public void clearCache() {
		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.remove("authentication_token");
		editor.commit();
		user = null;
		places = null;
		
		DataManager.getInstance().deleteDB();
	}
	
	
	public void logout(){
		KisiAccountManager.getInstance().deleteAccountByName(KisiAPI.getInstance().getUser().getEmail());
		clearCache();
		KisiRestClient.getInstance().delete("/users/sign_out",  new TextHttpResponseHandler() {
			public void onSuccess(String msg) {
	
			}
		});
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
	
	
	public User getUser() {
		return user;
	}

	
	public void updatePlaces(final OnPlaceChangedListener listener) {
		KisiRestClient.getInstance().get(context, "places",  new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Place[]  pl = gson.fromJson(response.toString(), Place[].class);
				DataManager.getInstance().savePlaces(pl);
				places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
				listener.onPlaceChanged(places);
				notifyAllOnPlaceChangedListener();		
			}
			
		});
	}
	
	
	//TODO: security
	public String getAuthToken() {
		//SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
	//	return settings.getString("authentication_token", "" );
		return user.getAuthentication_token();
	}
	
	
	public void updateLocks(final Place place, final OnPlaceChangedListener listener) {	
		KisiRestClient.getInstance().get(context, "places/" + String.valueOf(place.getId()) + "/locks",  new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Lock[] locks = gson.fromJson(response.toString(), Lock[].class);
				for(Lock l: locks) {
					l.setPlace(instance.getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocks(locks);
				listener.onPlaceChanged(places);
				notifyAllOnPlaceChangedListener();
			}
		});		
	}
	

	public void createNewKey(Place p, String email, List<Lock> locks) {

		JSONArray lock_ids = new JSONArray();
		for (Lock l : locks) {
			lock_ids.put(l.getId());
		}
		JSONObject key = new JSONObject();
		JSONObject data = new JSONObject();
		//changed in the API from assignee_email to issued_to_email
		try {
			key.put("lock_ids", lock_ids);
			key.put("issued_to_email", email);
			data.put("key", key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = "places/" + String.valueOf(p.getId()) + "/keys";
		
		KisiRestClient.getInstance().post(context, url, data, new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject data) {
				//TODO: Create Callback for Feedback
				/*try {
					Toast.makeText(
							activity,
							String.format(context.getResources().getString(R.string.share_success),
								data.getString("issued_to_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}*/
			}
			
		});	
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
		return place.getOwnerId()==this.user.getId();
	}
	
	public void unlock(Lock lock, final UnlockCallback callback){
        KisiLocationManager locationManager = KisiLocationManager.getInstance();
		Location currentLocation = locationManager.getCurrentLocation();;
		JSONObject location = new JSONObject();
		JSONObject data = new JSONObject();
		if(currentLocation != null) {
			try {
				location.put("latitude", currentLocation.getLatitude());
				location.put("longitude", currentLocation.getLongitude());
				data.put("location", location);
			} catch (JSONException e) {
			e.printStackTrace();
			}
		}
		else {
			try {
				location.put("latitude", 0.0);
				location.put("longitude", 0.0);
				data.put("location", location);
			} catch (JSONException e) {
			e.printStackTrace();
			}
			
		}
		String url = String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId());
		
		KisiRestClient.getInstance().post(context, url, data,  new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject response) {
				String message = null;
				if(response.has("notice")) {
					// change button design
					try {
						message = response.getString("notice");
					} catch (JSONException e) {
						e.printStackTrace();
					}	
				}
				callback.onUnlockSuccess(message);
			}
			
			public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
				//statusCode  == 0: no network connectivity
				String errormessage = null;
				if(statusCode == 0) {
					 errormessage = context.getResources().getString(R.string.no_network);
					 callback.onUnlockFail(errormessage);
					 return;
				 }
				
				if(errorResponse != null) {
					if(errorResponse.has("alert")) {
						try {
							errormessage = errorResponse.getString("alert");
						} catch (JSONException je) {
							e.printStackTrace();
						}
					}
				}
				else {
					errormessage = "Error!";
				}
				callback.onUnlockFail(errormessage);
			}	
		});
	}
	
	
	public void refresh(OnPlaceChangedListener listener) {
		DataManager.getInstance().deleteDB();
		this.updatePlaces(listener);
	}
	
	

	public void createGateway(JSONObject blinkUpResponse) {	
		String agentUrl = null;
		String impeeId = null;
		String planId = null;
		try {
			agentUrl = blinkUpResponse.getString("agent_url");
			impeeId = blinkUpResponse.getString("impee_id");
			planId = blinkUpResponse.getString("plan_id");
		} catch (JSONException e2) {
			e2.printStackTrace();
		} 
		//impeeId contains white spaces in the end, remove them
        if (impeeId != null) 
            impeeId = impeeId.trim();

        JSONObject location = new JSONObject();
        KisiLocationManager locationManager = KisiLocationManager.getInstance();
		Location currentLocation = locationManager.getCurrentLocation();
        try {
    		if(currentLocation != null) {
    			location.put("latitude", currentLocation.getLatitude());
    			location.put("longitude", currentLocation.getLongitude());
    		} else { //send 0.0 if location permission is revoked 
    			location.put("latitude", 0.0);
    			location.put("longitude", 0.0);
    		}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("plattform_name", "Android");
			gateway.put("plattform_version", Build.VERSION.RELEASE);
			gateway.put("blinked_up", true);
			gateway.put("ei_impee_id", impeeId);
			gateway.put("device_model", Build.MANUFACTURER + " " + Build.MODEL);
			gateway.put("location", location);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
		
		JSONObject data = new JSONObject();
		
		try {
			data.put("gateway", gateway);
			data.put("ei_plan_id", planId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		//TODO: Implement a proper handler
		KisiRestClient.getInstance().post(context, "gateways", data, new JsonHttpResponseHandler() {});	
		
	}
	
	
}
