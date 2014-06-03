package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;

public class UnlockCall extends GenericCall {

	public UnlockCall(Lock lock, final UnlockCallback callback) {
		super(String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId()), HTTPMethod.POST);
		
		handler = new JsonHttpResponseHandler() {
			
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
					 errormessage = KisiApplication.getInstance().getResources().getString(R.string.no_network);
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
		};
	}

	@Override
	protected void createJson() {
		JSONObject location =  KisiAPI.getInstance().generateJSONLocation();
		JSONObject data = new JSONObject();
		try {
			data.put("location", location);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		json = data;
	}
}
