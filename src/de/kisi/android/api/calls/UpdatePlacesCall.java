package de.kisi.android.api.calls;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.LockHandler;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Place;
import de.kisi.android.rest.KisiRestClient;

public class UpdatePlacesCall extends GenericCall {

	public UpdatePlacesCall(final OnPlaceChangedListener listener) {
		super("places", HTTPMethod.GET);
		this.handler = new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Place[]  pl = new Gson().fromJson(response.toString(), Place[].class);
				DataManager.getInstance().savePlaces(pl);
				//update locks for places
				for(Place p: pl) {
					LockHandler.getInstance().updateLocks(p, listener);
				}
			}
		};
	}

//	public void update<T>() {
//		T[] t = new new Gson().fromJson(response.toString(), T[].class);
//		specificStuff(t);
//		
//	}
}
