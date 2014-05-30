package de.kisi.android.api;

import android.content.Context;

import com.loopj.android.http.TextHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.calls.LoginCall;
import de.kisi.android.db.DataManager;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.rest.KisiRestClient;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

public class LoginHandler {
	
	private Context context;
	private static LoginHandler instance;
	
	public static LoginHandler getInstance(){
		if(instance == null)
			instance = new LoginHandler(KisiApplication.getInstance());
		return instance;
	}

	private LoginHandler(Context context){
		this.context = context;
	}

	public void login(String login, String password, final LoginCallback callback){
		
		new LoginCall(login, password, callback).send();
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
	
	public void clearCache() {		
		DataManager.getInstance().deleteDB();
	}
}
