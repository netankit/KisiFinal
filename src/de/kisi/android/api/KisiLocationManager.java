package de.kisi.android.api;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;



//TODO: evaluate with Location Provider should be used and how the best location is estimated  
//TODO: https://developer.android.com/guide/topics/location/strategies.html#BestEstimate
//TODO: stop this after is's done or the app is in background
public class KisiLocationManager implements LocationListener{
	
	private static KisiLocationManager instance;
	
	private Context context;
	private LocationManager locationManager;
	private Location currentLocation;
	private String locationProvider;
	
	public static void initialize(Context context){
		instance = new KisiLocationManager(context);
	}
	public static KisiLocationManager getInstance(){
		return instance;
	}

	private KisiLocationManager(Context context){
		this.context = context;
		locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		// first check Network Connection
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))  
			locationProvider = LocationManager.NETWORK_PROVIDER;
		// then the GPS Connection
		else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
			locationProvider = LocationManager.GPS_PROVIDER;
		
		currentLocation = locationManager.getLastKnownLocation(locationProvider);
		locationManager.requestLocationUpdates(locationProvider, 0, 0, this);

	}
	
	public Location getCurrentLocation() {
		return currentLocation;
	}
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = locationManager.getLastKnownLocation(locationProvider);		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	
}
