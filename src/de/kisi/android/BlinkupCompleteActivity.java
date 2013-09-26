package de.kisi.android;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.TokenStatusCallback;

public class BlinkupCompleteActivity extends Activity implements TokenStatusCallback {
	private BlinkupController blinkup;
	private LocationManager locationManager;
	private Location currentLocation;      
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blinkup);  
        blinkup = BlinkupController.getInstance();
        blinkup.getTokenStatus(this);
    }

	@Override
	public void onSuccess(Date verifiedDate, String agentUrl) {
	 	KisiApi api = new KisiApi(this);
    	api.setLoadingMessage("Gateway created");
    	updateLocation();
    	JSONObject location = new JSONObject();
    	//TODO:remove this later
    	Log.d("Blinkup Location","Latitude: "+ currentLocation.getLatitude() +  "\tLongitude: " + currentLocation.getLongitude());
    	try {
    		location.put("latitude", currentLocation.getLatitude());
	    	location.put("longitude", currentLocation.getLongitude());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("plattform_name", "Android");
			gateway.put("plattform_version", Build.VERSION.RELEASE);
			gateway.put("blinked", true);
			gateway.put("location", location);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	api.addParameter("gateway", gateway);
    	api.post("gateways");
    	Toast.makeText(getApplicationContext(), R.string.blinkup_success, Toast.LENGTH_LONG).show();
    	finish();	    
	}

	@Override
	public void onError(String errorMsg) {
		Toast.makeText(getApplicationContext(), R.string.blinkup_error + errorMsg, Toast.LENGTH_LONG).show();
    	finish();
	}

	@Override
	public void onTimeout() {
		Toast.makeText(getApplicationContext(), R.string.blinkup_timeout , Toast.LENGTH_LONG).show();
    	finish();
	}                            

	 private void updateLocation() {

			LocationListener locListener = new MyLocationListener();
			locationManager = (LocationManager) getApplicationContext().getSystemService(
					Context.LOCATION_SERVICE);
			// first check Network Connection
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { 
				locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locListener);
				Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				currentLocation = location;

			}
			// then the GPS Connection
			else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { 
				locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locListener);
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				currentLocation = location;
			}
			// TODO What happens if nothing of both is enabled?

		}                             



}
