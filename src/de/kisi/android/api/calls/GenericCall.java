package de.kisi.android.api.calls;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.rest.KisiRestClient;

public abstract class GenericCall {

	protected enum HTTPMethod {
		GET, POST
	}
	
	protected JsonHttpResponseHandler handler;
	protected String endpoint;
	protected JSONObject json;
	protected HTTPMethod method;
	
	protected GenericCall(String endpoint, HTTPMethod method) {
		this.endpoint = endpoint;
		this.method = method;
	}
	
	protected void createJson() {
		this.json = new JSONObject();
	}
	
	public void send() {
		final GenericCall call = this;
		this.createJson();
		JsonHttpResponseHandler realHandler = new JsonHttpResponseHandler() {
			//call = this
			
			@Override
			public void onSuccess(JSONArray response) {
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
			KisiRestClient.getInstance().get(this.endpoint, realHandler);
			break;
		case POST:
			KisiRestClient.getInstance().post(this.endpoint, this.json, realHandler);
			break;
		default:
			//TODO: do some error handling
			break;
		}
		
	}
	
}
