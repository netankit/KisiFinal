package de.kisi.android.api.calls;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class UpdateLocksCall extends GenericCall{

	public UpdateLocksCall(final Place place, final OnPlaceChangedListener listener) {
		super(String.format("places/%d/locks", place.getId()), HTTPMethod.GET);

		this.handler = new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Lock[] locks = gson.fromJson(response.toString(), Lock[].class);
				for(Lock l: locks) {
					l.setPlace(KisiAPI.getInstance().getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocks(locks);
				listener.onPlaceChanged(KisiAPI.getInstance().getPlaces());
				KisiAPI.getInstance().notifyAllOnPlaceChangedListener();
				//get also locators for this place
				KisiAPI.getInstance().updateLocators(place);
			}
		};
	}	
}
