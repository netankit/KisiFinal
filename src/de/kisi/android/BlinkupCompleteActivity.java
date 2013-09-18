package de.kisi.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.TokenStatusCallback;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class BlinkupCompleteActivity extends Activity {
	private BlinkupController blinkup;

    private TokenStatusCallback callback = new TokenStatusCallback() {

        public void onError(String errorMsg) {
        	Toast.makeText(getApplicationContext(), R.string.blinkup_error + errorMsg, Toast.LENGTH_LONG).show();
        	finish();
        }
        

        @Override
        public void onTimeout() {
        	Toast.makeText(getApplicationContext(), R.string.blinkup_timeout , Toast.LENGTH_LONG).show();
        	finish();
        }

		@Override
		public void onSuccess(Date verifiedDate, String agentUrl) {
			createGateway(agentUrl);
		}

    };
    

    
    private void createGateway(String agentUrl) {
    	KisiApi api = new KisiApi(this);
    	api.setLoadingMessage("Gateway created");
    	
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("plattform_name", "Android");
			gateway.put("plattform_version", Build.VERSION.RELEASE + " " + Build.VERSION.CODENAME);
			gateway.put("blinked", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	api.addParameter("gateway", gateway);
    	api.post("gateways");
		finish();	
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blinkup = BlinkupController.getInstance();
        blinkup.getTokenStatus(callback);
    }                            
}
