package de.kisi.android.api.calls;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.RegisterCallback;

public class RegisterCall extends GenericCall {

	private String user_email; 
	private String password;
	private String password_confirmation; 
	private Boolean terms_and_conditions;
	private final RegisterCallback callback;
	
	public RegisterCall(
			String user_email, 
			String password,
			String password_confirmation, 
			Boolean terms_and_conditions,
			final RegisterCallback callback) {
		
		super("users", HTTPMethod.POST);
		
		this.user_email = user_email;
		this.password = password;
		this.password_confirmation = password_confirmation;
		this.terms_and_conditions = terms_and_conditions;
		this.callback = callback;
		
		this.handler = new JsonHttpResponseHandler() {

			public void onSuccess(JSONObject response) {
				try {
					String id = response.getString("id");
					if (id != null && (!id.isEmpty())) {
						callback.onRegisterSuccess();
						return;
					}
				} catch (JSONException ej) {
					ej.printStackTrace();
				}
				callback.onRegisterFail("Error!");
				return;
			}

			public void onFailure(int statusCode, Throwable e,
					JSONObject response) {
				String errormessage = "";
				// no network connectivity
				if (statusCode == 0) {
					errormessage = KisiApplication.getInstance().getResources().getString(
							R.string.no_network);
					callback.onRegisterFail(errormessage);
					return;
				}

				if (response != null) {
					try {
						JSONObject errors = response
								.getJSONObject("errors");
						Iterator it = errors.keys();

						while (it.hasNext()) {
							String key = (String) it.next();
							String value = errors.getString(key);
							errormessage = errormessage + key + value
									+ '\n';
						}
						;
					} catch (JSONException ej) {
						ej.printStackTrace();
						errormessage = "Error!";
					}
				} else {
					errormessage = "Error!";
				}
				callback.onRegisterFail(errormessage);
				return;
			};

		};
		
	}

	
	@Override
	protected void createJson() {
		String deviceUUID = KisiAccountManager.getInstance().getDeviceUUID(user_email);

		JSONObject registerJSON = new JSONObject();
		JSONObject userJSON = new JSONObject();
		JSONObject deviceJSON = new JSONObject();
		try {
			// build user object
			userJSON.put("email", user_email);
			userJSON.put("password", password);
			userJSON.put("terms_and_conditions",terms_and_conditions == true ? "1" : "0");
			registerJSON.put("user", userJSON);

			// build device object
			if (deviceUUID != null) {
				deviceJSON.put("uuid", deviceUUID);
			}
			deviceJSON.put("platform_name", "Android");
			deviceJSON.put("platform_version", Build.VERSION.RELEASE);
			deviceJSON.put("model", Build.MANUFACTURER + " " + Build.MODEL);
			try {
				deviceJSON.put("app_version", KisiApplication.getInstance().getVersion());
			} catch (NameNotFoundException e) {
				// no app version for you then...
			}
			registerJSON.put("device", deviceJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}
	
	
}
