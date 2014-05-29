package de.kisi.android.api;


import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.rest.KisiRestClient;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;


public class KisiAPI {

	private static KisiAPI instance;  
	
	
	
	private Context context;
	
	public static KisiAPI getInstance(){
		if(instance == null)
			instance = new KisiAPI(KisiApplication.getInstance());
		return instance;
	}

	private KisiAPI(Context context){
		this.context = context;
	}
	
	
	public User getUser() {
		return 	DataManager.getInstance().getUser();
	}

	
	public void updateLocks(final Place place, final OnPlaceChangedListener listener) {	
		KisiRestClient.getInstance().get("places/" + String.valueOf(place.getId()) + "/locks",  new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Lock[] locks = gson.fromJson(response.toString(), Lock[].class);
				for(Lock l: locks) {
					l.setPlace(PlacesHandler.getInstance().getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocks(locks);
				listener.onPlaceChanged(PlacesHandler.getInstance().getPlaces());
				PlacesHandler.getInstance().notifyAllOnPlaceChangedListener();
				//get also locators for this place
				LocatorHandler.getInstance().updateLocators(place);
			}
		});		
	}
	
	//helper method for updateLocators
	public Lock getLockById(Place place, int lockId){ 
		List<Lock> locks = place.getLocks();
		if(locks == null)
			return null;
		for(Lock lock: locks) {
			if(lock.getId() == lockId) {
				return lock;
			}
		}
		return null;
	}
	
	
	public Lock getLockById(int lockId) {
		Place[] places = PlacesHandler.getInstance().getPlaces();
		if(places == null)
			return null;
		
		for(Place place: places){
			for(Lock lock: place.getLocks()) {
				if(lock.getId() == lockId){
					return lock;
				}
			}
		}
		return null;
	}
	
	public boolean createNewKey(Place p, String email, List<Lock> locks, final Activity activity) {

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
		
		KisiRestClient.getInstance().post(url, data, new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject data) {
				try {
					Toast.makeText(
							activity,
							String.format(context.getResources().getString(R.string.share_success),
								data.getString("issued_to_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		});	
		return true;
	}
	
	/**
	 * Send a request to the server to unlock this lock. The callback will
	 * run on another Thread, so do no direct UI modifications in there
	 * 
	 * @param lock The lock that should be unlocked
	 * @param callback Callback object for feedback, or null if no feedback is requested
	 */
	public void unlock(Lock lock, final UnlockCallback callback){
        JSONObject location =  generateJSONLocation();
		JSONObject data = new JSONObject();
		try {
			data.put("location", location);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String url = String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId());
		
		KisiRestClient.getInstance().post(url, data,  new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject response) {
				String message = null;
				if(response.has("notice")) {
					try {
						message = response.getString("notice");
					} catch (JSONException e) {
						e.printStackTrace();
					}	
				}
				if(callback != null)
					callback.onUnlockSuccess(message);
			}
			
			public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
				//statusCode  == 0: no network connectivity
				String errormessage = null;
				if(statusCode == 0) {
					 errormessage = context.getResources().getString(R.string.no_network);
					 if(callback != null)
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
				if(callback != null)
					callback.onUnlockFail(errormessage);
			}	
		});
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

        JSONObject location = generateJSONLocation();
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("blinked_up", true);
			gateway.put("ei_impee_id", impeeId);
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
		KisiRestClient.getInstance().post("gateways", data, new JsonHttpResponseHandler() {});
	}

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
	
	
	public void getLatestVerion(final VersionCheckCallback callback) {
		KisiRestClient.getInstance().get("stats", new JsonHttpResponseHandler() {

			public void onSuccess(org.json.JSONObject response) {
				String result = null;
				JSONObject JsonAndroid = null;
				try {
					JsonAndroid = (JSONObject) response.get("android");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if(JsonAndroid != null) {
					try {
						result = JsonAndroid.getString("latest_version");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				callback.onVersionResult(result);
			}

			public void onFailure(java.lang.Throwable e, org.json.JSONArray errorResponse) {
				callback.onVersionResult("error");
			}
		});
	}
}
