package de.kisi.android.ui;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.TokenStatusCallback;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;

public class BlinkupCompleteActivity extends Activity implements TokenStatusCallback {
	private BlinkupController blinkup; 
	
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
    

    @Override
	public void onSuccess(JSONObject json) { 
		//prevent that blickup sdk calls onSuccess twice
		blinkup.cancelTokenStatusPolling();
		//TODO:create a proper handler
		kisiAPI.createGateway(json);
    	status.setText(R.string.blinkup_success);
		status.setGravity(Gravity.CENTER);
    	progressBar.setVisibility(View.GONE);
	}

    
    //TODO:send logs to new relic
    @Override
	public void onError(String errorMsg) {
		status.setText(getResources().getString(R.string.blinkup_error));
		NewRelic.startInteraction(this, errorMsg);
		progressBar.setVisibility(View.GONE);
	}

    @Override
	public void onTimeout() {
		status.setText(getResources().getString(R.string.blinkup_error) );
		progressBar.setVisibility(View.GONE);
	} 
	


}
