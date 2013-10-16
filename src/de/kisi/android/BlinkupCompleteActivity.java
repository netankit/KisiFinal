package de.kisi.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.TokenStatusCallback;

public class BlinkupCompleteActivity extends Activity implements TokenStatusCallback {
	private BlinkupController blinkup;
	private LocationManager locationManager;
	private Location currentLocation;      
	
	private ProgressBar progressBar;
    private TextView status;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blinkup);  
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        status = (TextView) findViewById(R.id.status);
        blinkup = BlinkupController.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blinkup.getTokenStatus(this);
    }

	@Override
	public void onSuccess(JSONObject json) { 
		String agentUrl = null;
		String impeeId = null;
		String planId = null;
		try {
			agentUrl = json.getString("agent_url");
			impeeId = json.getString("impeeId");
			planId = json.getString("planId");
		} catch (JSONException e2) {
			e2.printStackTrace();
		} 
		
		KisiApi api = new KisiApi(this);
    	api.setLoadingMessage("Gateway created");
    	updateLocation();
    	JSONObject location = new JSONObject();
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
			gateway.put("ei_impee_id", impeeId);
			gateway.put("ei_plan_id", planId);
			gateway.put("location", location);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	api.addParameter("gateway", gateway);
    	api.post("gateways");

    	status.setText(R.string.blinkup_success);
		progressBar.setVisibility(View.GONE);   
	}

	@Override
	public void onError(String errorMsg) {
		status.setText( R.string.blinkup_error + errorMsg);
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onTimeout() {
		status.setText( R.string.blinkup_timeout);
		progressBar.setVisibility(View.GONE);
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
			Log.d("updateLocation", "Could not get location");
		}



}
