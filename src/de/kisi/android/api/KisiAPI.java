package de.kisi.android.api;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.User;
import de.kisi.android.rest.KisiRestClient;
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
