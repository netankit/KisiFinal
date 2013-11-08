package de.kisi.android;

import org.json.JSONException;
import org.json.JSONObject;

import com.manavo.rest.RestApi;

import android.app.Activity;
import android.content.SharedPreferences;

//TODO: tk: I would like to see an API class that is a singleton and that is not re-instantiated for every call
//TODO: tk: I NEED to see an API class that does not take strings from outside as request paths
public class KisiApi extends RestApi {
	
	private static String authToken;
	private static int user_id;

	public KisiApi(Activity activity) {
		super(activity);

		SharedPreferences settings = activity.getSharedPreferences("Config", android.content.Context.MODE_PRIVATE);
		KisiApi.authToken = settings.getString("authentication_token", "");
		KisiApi.user_id = settings.getInt("user_id", -1);

		this.BASE_URL = "https://www.kisi.de/";
		this.urlSuffix = ".json" + ( KisiApi.authToken != null ? "?auth_token=" + KisiApi.authToken : "" );
		//TODO: tk: why these next two lines? This is both already in the BASE_URL.
		//Is this REST library that bad or does it need proper usage?
		this.rest.setHost("www.kisi.de");
		this.rest.setPort(443);
		this.setUserAgent("AKey");
	}

	public static String getAuthToken() {
		return KisiApi.authToken;
	}
	public static int getUserId() {
		return KisiApi.user_id;
	}
	
	// return class instance so we can chain calls
	public KisiApi authorize(String email, String password) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("password", password);
			this.addParameter("user", (Object) data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return this;
	}

	@Override // to make method public
	public void get(String url) {
		super.get(url);
	}
	
	@Override 
	public void post(String url) {
		if ( this.getParameters().size() > 0 ) {
			this.rest.setContentType("application/json");
		}
		super.post(url);
	}
	
	@Override // to make method public
	public void delete(String url) {
		super.delete(url);
	}
	
	@Override 
	public void onStatusCodeError(int code, String data) {
		if (this.errorCallback == null && data.startsWith("{") ) {
			try {
				JSONObject data_json = new JSONObject(data);
				data = data_json.getString("error");
				if (data.isEmpty()) {
					data = "Unexpected error: " + String.valueOf(code);
				}
			} catch (JSONException e) {}
		}
		super.onStatusCodeError(code, data);
	}
}
