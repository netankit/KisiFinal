package de.kisi.android.vicinity.manager;

import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
/**
 * GeofenceManager is realized as a Singleton.
 * It has to be initialized before it can be used.
 * The GeofenceManager creates for every Place a Geofence.
 * On entering or leaving such a Geofence a VicinityActor will 
 * be activated.
 * 
 * @author Thomas Hörmann
 *
 */
public class GeofenceManager implements GooglePlayServicesClient.ConnectionCallbacks,
										GooglePlayServicesClient.OnConnectionFailedListener,
										OnAddGeofencesResultListener,
										OnPlaceChangedListener
										{
	// Instance for Singleton Access
	private static GeofenceManager instance;
	
	/**
	 * Starts the GeofenceManager and register all Places.
	 *  
	 * @param context The Application Context
	 */
	public static void initialize(Context context){
		if(instance == null)
			instance = new GeofenceManager(context);
	}
	/**
	 * Retrieve the singleton instance of the GeofenceManager.
	 * 
	 * @return Returns the GeofenceManager or NULL if initialize wasn't called
	 */
	public static GeofenceManager getInstance(){
		return instance;
	}

	
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private Context mContext;
	private boolean initialized = false;
	private int reconnectTries = 0;
	private List<String> geofenceIds = new LinkedList<String>();
	
	private GeofenceManager(Context context){
		mContext = context;
		mLocationClient = new LocationClient(context, this, this);
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
		
		mLocationRequest = LocationRequest.create();

		// Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(15000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);
        
        KisiAPI.getInstance().registerOnPlaceChangedListener(this);
        Place[] places = KisiAPI.getInstance().getPlaces();
        registerGeofences(places);
		
	
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

	@Override
	public void onPlaceChanged(Place[] newPlaces) {
		if(initialized){
			mLocationClient.removeGeofences(geofenceIds, null);
			geofenceIds.clear();
			registerGeofences(newPlaces);
		}else{
			mLocationClient.connect();
		}
	}

	private void registerGeofences(Place[] places){
        List<Geofence> fences = new LinkedList<Geofence>();

        // Create a Geofence for every Place
        for(Place p : places)
        {
        	fences.add(new Geofence.Builder()
        	.setRequestId("Place: "+p.getId())
        	.setCircularRegion(p.getLatitude(), p.getLongitude(), 50)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(1000*60*60*24)// one day
            .build());
        }
        
        // Register Geofences to the OS
        mLocationClient.addGeofences(fences, getPendingIntent(), this);
	}
}
