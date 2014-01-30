package de.kisi.android.vicinity.manager;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver receives Geofence Transitions defined by the 
 * Google Play Services
 * For more information see
 * http://developer.android.com/training/location/geofencing.html 
 */
public class GeofenceUpdateLocationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// get the actor for Geofence
		LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.Geofence);
		
		// get a list of all affected geofences
		List<Geofence> fences = LocationClient.getTriggeringGeofences(intent);
		
		// act for the transition
		int transitionType = LocationClient.getGeofenceTransition(intent);
		if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
            for(Geofence fence : fences){
            	String p[] = fence.getRequestId().split(": ");
            	int placeID = Integer.parseInt(p[1]);
            	actor.actOnEntry(placeID,0);
            }
        else if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
            for(Geofence fence : fences){
            	String p[] = fence.getRequestId().split(": ");
            	int placeID = Integer.parseInt(p[1]);
            	actor.actOnExit(placeID,0);
            }
	}

}
