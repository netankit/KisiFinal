package de.kisi.android.api;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;
import de.kisi.android.rest.KisiRestClient;

public class LocatorHandler {
	private PlacesHandler placesHandler;
	
	public void updateLocators(final Place place) {
		KisiRestClient.getInstance().get("places/" + String.valueOf(place.getId()) + "/locators", new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Locator[] locators = gson.fromJson(response.toString(), Locator[].class);
				for(Locator l: locators) {
					l.setLock(KisiAPI.getInstance().getLockById(PlacesHandler.getInstance().getPlaceById(l.getPlaceId()), l.getLockId()));
					l.setPlace(PlacesHandler.getInstance().getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocators(locators);
				getPlacesHandler().notifyAllOnPlaceChangedListener();
			}
			
		});
	}

	public PlacesHandler getPlacesHandler() {
		return placesHandler;
	}

	public void setPlacesHandler(PlacesHandler placesHandler) {
		this.placesHandler = placesHandler;
	}
}
