package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.google.gson.Gson;

import de.kisi.android.KisiApplication;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.GenericResponceHandler;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.User;
import de.kisi.android.rest.KisiRestClient;

public class LoginCall extends GenericCall {

	private String email;
	private String password;
	
	public LoginCall(String email, String password, final LoginCallback callback) {
		super("users/sign_in", HTTPMethod.POST);
		
		this.email = email;
		this.password = password;
		this.handler = new GenericResponceHandler(callback) {
			
			 public void onSuccess(org.json.JSONObject response) {
				Gson gson = new Gson();
				User user = gson.fromJson(response.toString(), User.class);
				DataManager.getInstance().saveUser(user);
				callback.onLoginSuccess(KisiAPI.getInstance().getUser().getAuthentication_token());
				return;
			}
		};
	}
	
	@Override
	protected void createJson() {
		super.createJson();
		String deviceUUID = KisiAccountManager.getInstance().getDeviceUUID(this.email);
		
		JSONObject userJSON = new JSONObject();
		JSONObject deviceJSON = new JSONObject();
		try {
			//build user object
			userJSON.put("email", this.email);
			userJSON.put("password", this.password);
			this.json.put("user", userJSON);
			
			//build device object
			if (deviceUUID != null) {
				deviceJSON.put("uuid", deviceUUID);				
			}
			deviceJSON.put("platform_name", "Android");
			deviceJSON.put("platform_version", Build.VERSION.RELEASE);
			deviceJSON.put("model", Build.MANUFACTURER + " " + Build.MODEL);
			try {
				deviceJSON.put("app_version", KisiApplication.getInstance().getVersion());
			} catch (NameNotFoundException e) {
				//no app version for you then...
			}
			this.json.put("device", deviceJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

	}
}
