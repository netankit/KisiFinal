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
import android.util.Log;

public class GeofenceUpdateLocationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent intent) {
		LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.Geofence);
		List<Geofence> fences = LocationClient.getTriggeringGeofences(intent);
		int transitionType = LocationClient.getGeofenceTransition(intent);
        Log.i("GeofenceManager","transition: "+transitionType);
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
