package de.kisi.android.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;



import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.*;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.User;

public class KisiRestClient {

	private static KisiRestClient instance;
	
	private static final String BASE_URL = "https://www.kisi.de/";
	private static final String URL_SUFFIX = ".json";
	
	private  AsyncHttpClient client;
	
	public static KisiRestClient getInstance() {
		if(instance == null){
			instance =  new KisiRestClient();
		}
		return instance;
	}
	
	private KisiRestClient() {
		 client = new AsyncHttpClient();
		 client.setCookieStore(new BlackholeCookieStore());
		 client.setUserAgent("de.kisi.android");
	}



	public void get(String url, AsyncHttpResponseHandler responseHandler) {
		String authToken = null;
		if(KisiAPI.getInstance().getUser() != null) {
			authToken = KisiAPI.getInstance().getUser().getAuthentication_token();
		}
		if(authToken != null) {
			client.get(getAbsoluteUrl(url, authToken), responseHandler);
		}
		else {
			client.get(getAbsoluteUrl(url), responseHandler);
		}
	}

	public void post(String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		String authToken = null;
		if(KisiAPI.getInstance().getUser() != null) {
			authToken = KisiAPI.getInstance().getUser().getAuthentication_token();
		}
		if(authToken != null) {
			client.post(KisiApplication.getApplicationInstance(), getAbsoluteUrl(url, authToken), JSONtoStringEntity(data), "application/json", responseHandler);
		}
		else {
			client.post(KisiApplication.getApplicationInstance(), getAbsoluteUrl(url), JSONtoStringEntity(data), "application/json", responseHandler);
		}
	}
	
	//workaround method so that the login call is done without the authtoken and so renews the old authtoken
	public void postWithoutAuthToken (String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		client.post(KisiApplication.getApplicationInstance(), getAbsoluteUrl(url), JSONtoStringEntity(data), "application/json", responseHandler);
	}

	
	
	public  void delete(String url, AsyncHttpResponseHandler responseHandler) {
		String authToken = null;
		if(KisiAPI.getInstance().getUser() != null) {
			authToken = KisiAPI.getInstance().getUser().getAuthentication_token();
			Log.i("KisiRestClient",getAbsoluteUrl(url,authToken));
			client.delete(getAbsoluteUrl(url,authToken),  responseHandler);
		}else{
			client.delete(getAbsoluteUrl(url),  responseHandler);
		}

	}

	//this method is for KisiAuthenticator.getAuthToken()
	public String signIn (String url, JSONObject data) throws ClientProtocolException, IOException { 
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(getAbsoluteUrl(url));
		httpPost.setEntity(JSONtoStringEntity(data));
		
		HttpResponse response;
		String responseString = null;
		//TODO: maybe should the method pass the exception
		response = httpClient.execute(httpPost);
		responseString = EntityUtils.toString(response.getEntity());
		Gson gson = new Gson();
		User user;
		user = gson.fromJson(responseString, User.class);
		
		return user.getAuthentication_token();

	}
	
	
	
	private  String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl + URL_SUFFIX;
	}
	
	private  String getAbsoluteUrl(String relativeUrl, String authToken) {
		return BASE_URL + relativeUrl + URL_SUFFIX + "?auth_token=" + authToken;
	}

	private  StringEntity JSONtoStringEntity (JSONObject json) {
		StringEntity entity = null;
        try {
			entity = new StringEntity(json.toString());
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return entity;
	}
	
	

	
}




