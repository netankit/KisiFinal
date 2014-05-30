package de.kisi.android.api.calls;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.LocatorHandler;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.api.PlacesHandler;
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
					l.setPlace(PlacesHandler.getInstance().getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocks(locks);
				listener.onPlaceChanged(PlacesHandler.getInstance().getPlaces());
				PlacesHandler.getInstance().notifyAllOnPlaceChangedListener();
				//get also locators for this place
				LocatorHandler.getInstance().updateLocators(place);
			}
		};
	}	
}
