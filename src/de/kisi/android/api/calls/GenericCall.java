package de.kisi.android.api.calls;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAuthenticator;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.rest.KisiRestClient;

public abstract class GenericCall {

	protected JsonHttpResponseHandler handler;
	protected String path;
	protected JSONObject json;
	protected HTTPMethod method;
	
	protected GenericCall(String path, HTTPMethod method) {
		this.path = path;
		this.method = method;
	}
	
	protected void createJson() {
		this.json = new JSONObject();
	}
	
	public void send() {
		final GenericCall call = this;
		createJson();
		JsonHttpResponseHandler realHandler = new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject response) {
				Log.d("GenericCall", "call success: " + response.toString());
				if (call.handler != null) {
					call.handler.onSuccess(response);
				}
			}	
			
			public void onFailure(int statusCode, Throwable e, JSONObject response) {
				Log.d("GenericCall", "call failed: "+response.toString());
				if (loggedOut(statusCode)) {
					
				} else if (call.handler != null) {
					call.handler.onFailure(statusCode, e, response);
				}
			}
			
			private void logout() {
				KisiAPI.getInstance().logout();
				Toast.makeText(KisiApplication.getInstance(), KisiApplication.getInstance().getResources()
						.getString(R.string.automatic_relogin_failed), Toast.LENGTH_LONG).show();
			}
			
			private boolean loggedOut(int statusCode) {
				if (statusCode == 401) {
					if (call instanceof LoginCall)
						logout();
					else { // retry

						AccountManager mAccountManager = AccountManager.get(KisiApplication.getInstance());
						Account availableAccounts[] = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);
						if(availableAccounts.length==0){
							logout();
						}
						Account acc = availableAccounts[0];
						for (Account a : availableAccounts)
							if (a.name.equals(KisiAPI.getInstance().getUser().getEmail()))
								acc = a;
						String password = mAccountManager.getPassword(acc);
						KisiAPI.getInstance().login(acc.name, password, new LoginCallback() {

							@Override
							public void onLoginSuccess(String authtoken) {
								call.send();
							}

							@Override
							public void onLoginFail(String errormessage) {
								if (!KisiApplication.getInstance().getResources().getString(R.string.no_network).equals(errormessage))
									logout();
							}

						});
					}
					return true;
				} else {
					return false;
				}
			}
			
			@Override
			public void onSuccess(JSONArray response) {
				Log.d("GenericCall", "call success: " + response.toString());
				if (call.handler != null) {
					call.handler.onSuccess(response);
				}
			}	
			
			public void onFailure(int statusCode, Throwable e, JSONArray response) {
				Log.d("GenericCall", "call failed: "+response.toString());
				if (loggedOut(statusCode)) {
					
				} else if (call.handler != null) {
					call.handler.onFailure(statusCode, e, response);
				}
				
			}
		};
		
		switch (this.method) {
		case GET:
			KisiRestClient.getInstance().get(path, realHandler);
			break;
		case POST:
			KisiRestClient.getInstance().post(path, json, realHandler);
			break;
		case DELETE:
			KisiRestClient.getInstance().delete(path,  realHandler);
			break;
		default:
			throw new RuntimeException("Unsupported HttpMethod");
		}
	}
}
