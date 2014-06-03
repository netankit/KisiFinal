package de.kisi.android.api.calls;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;

public class UpdateLocatorsCall extends GenericCall {

	public UpdateLocatorsCall(final Place place) {
		super("places/" + place.getId() + "/locators", HTTPMethod.GET);
		
		handler = new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Locator[] locators = gson.fromJson(response.toString(), Locator[].class);
				for(Locator locator: locators) {
					locator.setLock(KisiAPI.getInstance().getLockById(KisiAPI.getInstance().getPlaceById(locator.getPlaceId()), locator.getLockId()));
					locator.setPlace(KisiAPI.getInstance().getPlaceById(locator.getPlaceId()));
				}
				DataManager.getInstance().saveLocators(locators);
				KisiAPI.getInstance().notifyAllOnPlaceChangedListener();
			}
			
		};
	}

}
