package de.kisi.android.vicinity.manager;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

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
            	if(checkLocation(KisiAPI.getInstance().getPlaceById(placeID))){
            		actor.actOnEntry(placeID,0);
            	}
            }
        else if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
            for(Geofence fence : fences){
            	String p[] = fence.getRequestId().split(": ");
            	int placeID = Integer.parseInt(p[1]);
            	actor.actOnExit(placeID,0);
            }
	}
	
	//TODO: REMOVE NOTIFICATIONS FOR RELEASE
	//check if current location confirms the geofence by checking this formalar (Feature #515)
	//accuracy < 100 && (distance_to_place - accuracy) < 2*geofence_radius
	private boolean checkLocation(Place place) {
		Location currentLocation = GeofenceManager.getInstance().getLocation();
		Location placeLocation = new Location("mPlace");
		placeLocation.setLatitude(place.getLatitude());
		placeLocation.setLongitude(place.getLongitude());
		if(currentLocation != null) {
			if(currentLocation.getAccuracy() >= 100.0f) {
//				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//						KisiApplication.getApplicationInstance())
//						.setSmallIcon(R.drawable.notification_icon)
//						.setContentTitle("Geofence error. Place: " + place.getName())
//						.setContentText(
//								"The  inaccuracy is too large to triger geofence: Accuracy:  "
//										+ currentLocation.getAccuracy());
//				NotificationManager mNotificationManager = (NotificationManager) KisiApplication
//						.getApplicationInstance().getSystemService(
//								Context.NOTIFICATION_SERVICE);
//
//				mNotificationManager.notify(place.getId(), mBuilder.build());
				return false;
			} else if((currentLocation.distanceTo(placeLocation) - currentLocation.getAccuracy()) >= 2.0*GeofenceManager.GEOFENCE_RADIUS) {
//				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//						KisiApplication.getApplicationInstance())
//						.setSmallIcon(R.drawable.notification_icon)
//						.setContentTitle("Geofence error. Place: " + place.getName())
//						.setContentText(
//								"The current location is too far away from the geofence: Distance : "
//										+ currentLocation
//												.distanceTo(placeLocation)
//										+ " Accuracy : "
//										+ currentLocation.getAccuracy());
//				NotificationManager mNotificationManager = (NotificationManager) KisiApplication
//						.getApplicationInstance().getSystemService(
//								Context.NOTIFICATION_SERVICE);
//
//				mNotificationManager.notify(place.getId(), mBuilder.build());
				return false;
			} else {
				return true;
			}	
		} else {
			return false;
		}
	}

}
