package de.kisi.android;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.TokenStatusCallback;

import de.kisi.android.api.KisiAPI;

public class BlinkupCompleteActivity extends Activity implements TokenStatusCallback {
	private BlinkupController blinkup;
	private LocationManager locationManager; 
	
	private ProgressBar progressBar;
    private TextView status;
    
    
    private KisiAPI kisiAPI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blinkup);  
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        status = (TextView) findViewById(R.id.status);
        blinkup = BlinkupController.getInstance();
        kisiAPI = KisiAPI.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blinkup.getTokenStatus(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        blinkup.cancelTokenStatusPolling();
    }  
    


	public void onSuccess(JSONObject json) { 
		//prevent that blickup sdk calls onSuccess twice
		blinkup.cancelTokenStatusPolling();
		//TODO:create a proper handler
		kisiAPI.createGateway(json);
    	status.setText(R.string.blinkup_success);
		status.setGravity(Gravity.CENTER);
    	progressBar.setVisibility(View.GONE);
	}


	public void onError(String errorMsg) {
		status.setText(getResources().getString(R.string.blinkup_error) + " " + errorMsg);
		progressBar.setVisibility(View.GONE);
	}


	public void onTimeout() {
		status.setText(getResources().getString(R.string.blinkup_timeout) );
		progressBar.setVisibility(View.GONE);
	} 
	
	//TODO: tk: copy-paste code. Please unify in separate class.
	 private void updateLocation() {
			locationManager = (LocationManager) getApplicationContext().getSystemService(
					Context.LOCATION_SERVICE);
			// first check Network Connection
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { 
				locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
				Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			}
			// then the GPS Connection
			else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { 
				locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			// TODO What happens if nothing of both is enabled
		}


}
