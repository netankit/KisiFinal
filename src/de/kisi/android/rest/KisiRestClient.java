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

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.loopj.android.http.*;

import de.kisi.android.model.User;

public class KisiRestClient {

	private static KisiRestClient instance =  new KisiRestClient();
	
	private static final String BASE_URL = "https://www.kisi.de/";
	private static final String URL_SUFFIX = ".json";
	
	private  AsyncHttpClient client;
	
	public static KisiRestClient getInstance() {
		return instance;
	}
	
	private KisiRestClient() {
		 client = new AsyncHttpClient();
		 client.setCookieStore(new BlackholeCookieStore());
		 client.setUserAgent("Android_Kisi");
	}



	public void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {

		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		String authToken = settings.getString("authentication_token", "");
		if(authToken != null) 
			client.get(getAbsoluteUrl(url, authToken), responseHandler);
		else
			client.get(getAbsoluteUrl(url), responseHandler);
	}

	public void post(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		String authToken = settings.getString("authentication_token", "");
		if(authToken != null) 
			client.post(context, getAbsoluteUrl(url, authToken), JSONtoStringEntity(data), "application/json", responseHandler);
		else
			client.post(context, getAbsoluteUrl(url), JSONtoStringEntity(data), "application/json", responseHandler);
	}
	
	
	public  void delete(String url, AsyncHttpResponseHandler responseHandler) {
		client.delete(getAbsoluteUrl(url),  responseHandler);
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




