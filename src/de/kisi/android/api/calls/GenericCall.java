package de.kisi.android.api.calls;

import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

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
				Log.d("GenericCall", "call success");
				if (call.handler != null) {
					call.handler.onSuccess(response);
				}
			}	
			
			public void onFailure(int statusCode, Throwable e, JSONObject response) {
				if (call.handler != null) {
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
