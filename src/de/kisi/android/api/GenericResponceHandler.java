package de.kisi.android.api;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;

public abstract class GenericResponceHandler extends JsonHttpResponseHandler {
	private LoginCallback callback;
	
	public GenericResponceHandler(LoginCallback callback) {
		super();
		this.callback = callback;
	}
	
	
	public void onFailure(int statusCode, Throwable e, JSONObject response) {
		 String errormessage = null;
		 //no network connectivity
		 if(statusCode == 0) {
			 errormessage = KisiApplication.getInstance().getResources().getString(R.string.no_network);
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
	}

}
