package de.kisi.android.vicinity.manager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

/**
 * GeofenceManager is realized as a Singleton.
 * It has to be initialized before it can be used.
 * The GeofenceManager creates for every Place a Geofence.
 * On entering or leaving such a Geofence a VicinityActor will 
 * be activated.
 *
 */
public class GeofenceManager implements GooglePlayServicesClient.ConnectionCallbacks,
										GooglePlayServicesClient.OnConnectionFailedListener,
										OnAddGeofencesResultListener,
										OnPlaceChangedListener, LocationListener
										{
	// Instance for Singleton Access
	private static GeofenceManager instance;
	
	public static int GEOFENCE_RADIUS = 75;
	
	private HashSet<Integer> placeMap = new HashSet<Integer>();
	
	/**
	 * Retrieve the singleton instance of the GeofenceManager.
	 */
	public static GeofenceManager getInstance(){
		if(instance == null)
			instance = new GeofenceManager();
		return instance;
	}

	
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private Location mLocation;
	private Context mContext;
	private boolean initialized = false;
	private int reconnectTries = 0;
	private List<String> geofenceIds = new LinkedList<String>();
	

	private GeofenceManager(){
		mContext = KisiApplication.getInstance();
		mLocationClient = new LocationClient(mContext, this, this);
		mLocationClient.connect();
	}

	/**
	 * Create a PendingIntent used for GeofenceUpdateLocationReceiver update calls.
	 *
	 * @return 
	 */
	private PendingIntent getPendingIntent(){
		Intent i = new Intent(mContext,GeofenceUpdateLocationReceiver.class);
		return PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		if (LocationStatusCodes.SUCCESS == statusCode){
			geofenceIds.addAll(geofenceIds);
		}else
			initialized = false;
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		initialized = false;
		reconnect();
	}

	@Override
	public void onConnected(Bundle arg0) {
		initialized = true;
		reconnectTries = 0;
		startLocationUpdate();
        // Show interest on any change of the Places
		KisiAPI.getInstance().registerOnPlaceChangedListener(this);
        // Register Places as Geofences
        registerGeofences(KisiAPI.getInstance().getPlaces());
	}

	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
	}
	
	
	
	@Override
	public void onDisconnected() {
		initialized = false;
		reconnect();
	}

	private void reconnect(){
		if(reconnectTries < 3){
			reconnectTries++;
			mLocationClient = new LocationClient(mContext, this, this);
			mLocationClient.connect();
		}
	}

	/**
	 * Handle altered places 
	 */
	@Override
	public void onPlaceChanged(Place[] newPlaces) {
		if(initialized){
			if(geofenceIds.size()>0){
				mLocationClient.removeGeofences(geofenceIds, null);
				geofenceIds.clear();
			}
			registerGeofences(newPlaces);
		}else{
			mLocationClient.connect();
		}
	}

	private void registerGeofences(Place[] places){
		// If there are no Places to register, delete all geofences
		if(places.length==0){
			removeAllGeofences();
			return;
		}
		
        List<Geofence> fences = new LinkedList<Geofence>();
        @SuppressWarnings("unchecked")
		HashSet<Integer> removeSet = (HashSet<Integer>) placeMap.clone();
        
        // Create a Geofence for every Place
        for(Place p : places)
        {
        	removeSet.remove(p.getId());
        	//check if place already generated a geofence 
        	//this check must be done, because the API updates the place quite often on the startup and calls the onPlaceChanged
        	//and so the phone would vibrate the whole time
        	if(!placeMap.contains(p.getId())) {
        		placeMap.add(p.getId());
        		
        		// Create the Geofence
        		fences.add(new Geofence.Builder()
        		.setRequestId("Place: "+p.getId())
        		.setCircularRegion(p.getLatitude(), p.getLongitude(), GEOFENCE_RADIUS) 
        		.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_ENTER)
        		.setExpirationDuration(Geofence.NEVER_EXPIRE) //infinite  
        		.build());

        	}        	
        	
        }
        
        // remove the geofences that are not in the list anymore
        for(Integer placeId:removeSet)
        	removeGeofence(placeId);
        
        // Register Geofences to the OS
        if(!fences.isEmpty()) {
        	mLocationClient.addGeofences(fences, getPendingIntent(), this);
        }
	}
	
	/**
	 * This removes the corresponding geofence from a place 
	 * @param placeId Id of the Place
	 */
	public void removeGeofence(int placeId) {
		placeMap.remove(placeId);
		List<String> geofenceRequestIdsToRemove = new LinkedList<String>();
		geofenceRequestIdsToRemove.add("Place: " + placeId);
		mLocationClient.removeGeofences(geofenceRequestIdsToRemove, new OnRemoveGeofencesResultListener() {

			@Override
			public void onRemoveGeofencesByPendingIntentResult(int arg0,
					PendingIntent arg1) {
			}

			@Override
			public void onRemoveGeofencesByRequestIdsResult(int arg0,
					String[] arg1) {
			}
			
		});
	}
	
	/**
	 * Remove all registered geofences
	 */
	public void removeAllGeofences() {
		//LockInVicinityDisplayManager.getInstance().update();
		//placeMap.clear();
		mLocationClient.removeGeofences(getPendingIntent(),  new OnRemoveGeofencesResultListener() {

			@Override
			public void onRemoveGeofencesByPendingIntentResult(int arg0,
					PendingIntent arg1) {
			}

			@Override
			public void onRemoveGeofencesByRequestIdsResult(int arg0,
					String[] arg1) {
			}
			
		});
	}
	
	public void startLocationUpdate() {
		if(mLocationClient.isConnected()) {
			mLocationRequest = LocationRequest.create();
			// Use high accuracy
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        // Set the update interval to 15 seconds
	        mLocationRequest.setInterval(15000);
	        // Set the fastest update interval to 1 second
	        mLocationRequest.setFastestInterval(1000);
	        mLocation = mLocationClient.getLastLocation();
	        mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
	}
	
	public void stopLocationUpdate() {
		if(mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
	}
	
	
	public Location getLocation() {
		return mLocation;
	}


}
