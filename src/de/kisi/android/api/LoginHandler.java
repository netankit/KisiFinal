package de.kisi.android.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.User;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.rest.KisiRestClient;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

public class LoginHandler {
	
	private Context context;
	
	public LoginHandler(Context context) {
		this.context = context;
	}

	public void login(String login, String password, final LoginCallback callback){
		
		String deviceUUID = KisiAccountManager.getInstance().getDeviceUUID(login);
		
		JSONObject loginJSON = new JSONObject();
		JSONObject userJSON = new JSONObject();
		JSONObject deviceJSON = new JSONObject();
		try {
			//build user object
			userJSON.put("email", login);
			userJSON.put("password", password);
			loginJSON.put("user", userJSON);
			
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
			loginJSON.put("device", deviceJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		KisiRestClient.getInstance().postWithoutAuthToken("users/sign_in", loginJSON,  new JsonHttpResponseHandler() {
			
			 public void onSuccess(org.json.JSONObject response) {
				Gson gson = new Gson();
				User user = gson.fromJson(response.toString(), User.class);
				DataManager.getInstance().saveUser(user);
				callback.onLoginSuccess(KisiAPI.getInstance().getUser().getAuthentication_token());
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
	public void logout(){
		KisiAccountManager.getInstance().deleteAccountByName(KisiAPI.getInstance().getUser().getEmail());
		clearCache();
		BluetoothLEManager.getInstance().stopService();
		LockInVicinityDisplayManager.getInstance().update();
		NotificationManager.removeAllNotification();
		KisiRestClient.getInstance().delete("/users/sign_out",  new TextHttpResponseHandler() {
			public void onSuccess(String msg) {
	
			}
		});
	}
	
	private void clearCache() {		
		DataManager.getInstance().deleteDB();
	}
}
