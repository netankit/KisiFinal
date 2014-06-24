package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.VersionCheckCallback;

public class VersionCheckCall extends GenericCall {

	public VersionCheckCall(final VersionCheckCallback callback) {
		super("stats", HTTPMethod.GET);
		
		this.handler = new JsonHttpResponseHandler() {

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
		};
	}
}
