package de.kisi.android;

import de.kisi.android.api.KisiAPI;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;


public class LogInfo extends Activity implements OnClickListener {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.loginfo);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.log_title);

		WebView webView = (WebView) findViewById(R.id.webView);
		//Commented this to fix the bug after the target version update from sdk-version 18 -> 19 that the WebView was just show the word "none"
		//webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebClient());
	
		 // gets the previously created intent
		int place_id = getIntent().getIntExtra("place_id", 0);
		webView.loadUrl(String.format("https://kisi.de/places/%d/events?auth_token=%s", 
			place_id,  KisiAPI.getInstance().getUser().getAuthentication_token())
		);
		ImageButton backButton = (ImageButton) findViewById(R.id.back);

		backButton.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		finish();
	}
}
