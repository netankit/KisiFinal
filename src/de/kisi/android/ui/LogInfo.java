package de.kisi.android.ui;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class LogInfo extends Activity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loginfo);
		//add back button to action bar 
		getActionBar().setDisplayHomeAsUpEnabled(true);

		WebView webView = (WebView) findViewById(R.id.webView);
		//Commented this to fix the bug after the target version update from sdk-version 18 -> 19 that the WebView was just show the word "none"
		//webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebClient());
		 // gets the previously created intent
		int place_id = getIntent().getIntExtra("place_id", 0);
		webView.loadUrl(String.format("https://kisi.de/places/%d/events?auth_token=%s", 
			place_id,  KisiAPI.getInstance().getAuthToken()));

	}
	
	//listener for the backbutton of the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}
	
}
