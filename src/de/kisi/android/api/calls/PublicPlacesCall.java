package de.kisi.android.api.calls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.PublicPlacesCallback;
import de.kisi.android.model.Place;

/**
 * This call is sending the current position of the device to the server.
 * The response should contain a list of places, 
 * that are near your position and that you can ring.
 */
public class PublicPlacesCall extends LocatableCall{
	
	public PublicPlacesCall(final PublicPlacesCallback callback) {
		super("publicplaces", HTTPMethod.POST);//TODO: url might be different in final version
		
		//TODO: following lines are commented out until server side of this call is implemented
//		this.handler = new JsonHttpResponseHandler() { 
//			@Override
//			public void onFinish() {
//				Log.d("PublicPlacesCall.onFinish", "PublicPlacesCall finished");
//				super.onFinish();
//			}
//			public void onSuccess(JSONArray response) {
//				try {
//					Log.d("PublicPlacesCall.onSuccess", "PublicPlacesCall succeeded: "+response.toString(2));
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				Place[]  places = new Gson().fromJson(response.toString(), Place[].class);
//				callback.onResult(places);
//			}
//			
//			public void onFailure(Throwable e, JSONArray errorResponse) {
//				Log.d("PublicPlacesCall.onFailure", "PublicPlacesCall failed: x1");
//				callback.onResult(null);
//			}
//			
//			public void onFailure(int statusCode, Throwable e, JSONObject response) {
//				Log.d("PublicPlacesCall.onFailure", "PublicPlacesCall failed: x2");
//				callback.onResult(null);
//			}
//			
//			public void onFailure(int statusCode, Throwable e, JSONArray response) {
//				Log.d("PublicPlacesCall.onFailure", "PublicPlacesCall failed: x3");
//				callback.onResult(null);
//			}
//		};
		
		callback.onResult(getTestPlaces()); //TODO: remove when server side of this call is implemented
	}
	
	private static Place[] getTestPlaces(){ //TODO: remove when server side of this call is implemented
		String fakeResponse="[{\"address\":\"1 Teststreet, 81111 München, Germany\",\"short_address\":\"1 Teststreet, 81111 München\",\"id\":626,\"user_id\":2189,\"name\":\"Home of 1\",\"street_name\":\"Teststreet\",\"street_number\":\"1\",\"zip\":\"81111\",\"city\":\"München\",\"state\":null,\"country\":\"Germany\",\"description\":\"Welcome to Home of 1\",\"latitude\":48.1351,\"longitude\":11.582,\"profile_name\":null,\"profile_accessible\":null,\"profile_access_requestable\":null,\"profile_icon\":null,\"profile_description\":null,\"created_at\":\"2014-07-11T11:10:06.000Z\",\"updated_at\":\"2014-07-11T11:10:37.000Z\",\"enabled\":true,\"dropcam_url\":null,\"locator_id\":null,\"suggest_unlock\":true,\"user\":{\"email\":\"kisitest1@t-online.de\"},\"current_user_permissions\":{\"manage\":true,\"share\":true}},{\"address\":\"2 Teststreet, 82222 München, Germany\",\"short_address\":\"2 Teststreet, 82222 München\",\"id\":627,\"user_id\":2190,\"name\":\"Home of 2\",\"street_name\":\"Teststreet\",\"street_number\":\"2\",\"zip\":\"82222\",\"city\":\"München\",\"state\":null,\"country\":\"Germany\",\"description\":\"Welcome to Home of 2\",\"latitude\":48.1351,\"longitude\":11.582,\"profile_name\":null,\"profile_accessible\":null,\"profile_access_requestable\":null,\"profile_icon\":null,\"profile_description\":null,\"created_at\":\"2014-07-11T11:12:43.000Z\",\"updated_at\":\"2014-07-11T11:12:51.000Z\",\"enabled\":true,\"dropcam_url\":null,\"locator_id\":null,\"suggest_unlock\":true,\"user\":{\"email\":\"kisitest2@t-online.de\"},\"current_user_permissions\":{\"manage\":true,\"share\":true}}]";
		Place[]  places = new Gson().fromJson(fakeResponse, Place[].class);
		return places;
	}
}