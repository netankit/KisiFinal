package de.kisi.android.api;


import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.calls.CreateGatewayCall;
import de.kisi.android.api.calls.CreateNewKeyCall;
import de.kisi.android.api.calls.LoginCall;
import de.kisi.android.api.calls.LogoutCall;
import de.kisi.android.api.calls.UnlockCall;
import de.kisi.android.api.calls.UpdateLocatorsCall;
import de.kisi.android.api.calls.UpdateLocksCall;
import de.kisi.android.api.calls.UpdatePlacesCall;
import de.kisi.android.api.calls.VersionCheckCall;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;


public class KisiAPI {

	// -------------------- Singleton Stuff: --------------------
	private static KisiAPI instance;
	public static KisiAPI getInstance(){
		if(instance == null)
			instance = new KisiAPI();
		return instance;
	}
	private KisiAPI(){
	}
	
	
	// -------------------- CALLS: --------------------
	public void createGateway(JSONObject blinkUpResponse) {
		new CreateGatewayCall(blinkUpResponse).send();
	}
	
	public void getLatestVerion(final VersionCheckCallback callback) {
		new VersionCheckCall(callback).send();
	}
	
	public void updateLocators(final Place place) {
		new UpdateLocatorsCall(place).send();
	}
	
	public void updateLocks(final Place place, final OnPlaceChangedListener listener) {
		new UpdateLocksCall(place, listener).send();
	}
	
	public boolean createNewKey(Place place, String email, List<Lock> locks, final Activity activity) {
		new CreateNewKeyCall(place, email, locks, activity).send();
		return true;
	}
	
	public void login(String email, String password, final LoginCallback callback){
		new LoginCall(email, password, callback).send();
	}
	
	public void logout(){
		KisiAccountManager.getInstance().deleteAccountByName(KisiAPI.getInstance().getUser().getEmail());
		clearCache();
		BluetoothLEManager.getInstance().stopService();
		LockInVicinityDisplayManager.getInstance().update();
		NotificationManager.removeAllNotification();
		
		new LogoutCall().send();
	}
	
	public void updatePlaces(final OnPlaceChangedListener listener) {
		if(getUser() == null)
			return;

		new UpdatePlacesCall(listener).send();
	}
	
	/**
	 * Send a request to the server to unlock this lock. The callback will
	 * run on another Thread, so do no direct UI modifications in there
	 * 
	 * @param lock The lock that should be unlocked
	 * @param callback Callback object for feedback, or null if no feedback is requested
	 */
	public void unlock(Lock lock, final UnlockCallback callback){
		new UnlockCall(lock, callback).send();
	}
	
	
	// -------------------- DATA: --------------------
	public User getUser() {
		return 	DataManager.getInstance().getUser();
	}
	
	public Place[] getPlaces(){
		return DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
	}
	
	public Place getPlaceAt(int index){
		Place[] places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
		if(places != null && index>=0 && index<places.length)
			return places[index];
		return null;
	}
	
	public Place getPlaceById(int num){
		Place[]  places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
		for(Place p : places)
			if(p.getId() == num)
				return p;
		return null;
	}
	
	public void clearCache() {		
		DataManager.getInstance().deleteDB();
	}
	
	public Lock getLockById(Place place, int lockId){
		List<Lock> locks  = place.getLocks();
		if(locks == null)
			return null;
		for(Lock lock: locks) {
			if(lock.getId() == lockId) {
				return lock;
			}
		}
		return null;
	}
	
	public Lock getLockById(int lockId) {
		Place[] places = KisiAPI.getInstance().getPlaces();
		if(places == null)
			return null;
		
		for(Place place: places){
			for(Lock lock: place.getLocks()) {
				if(lock.getId() == lockId){
					return lock;
				}
			}
		}
		return null;
	}
	
	
	// -------------------- OTHER: --------------------
	public JSONObject generateJSONLocation() { // TODO: this should maybe be moved to GeofenceManager.
		JSONObject location = new JSONObject();
		Location currentLocation = GeofenceManager.getInstance().getLocation();
		try {
    		if(currentLocation != null) {
    			location.put("latitude", currentLocation.getLatitude());
    			location.put("longitude", currentLocation.getLongitude());
    			location.put("horizontal_accuracy", currentLocation.getAccuracy());
    			location.put("altitude", currentLocation.getAltitude());
    			location.put("age", (System.currentTimeMillis() - currentLocation.getTime())/1000.0);
    		} else { 
	 			location.put("error:", "Location data not accessible");
    		}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        return location;
	}
	
	public void refresh(OnPlaceChangedListener listener) {
		DataManager.getInstance().deletePlaceLockLocatorFromDB();
		this.updatePlaces(listener);
	}
}
