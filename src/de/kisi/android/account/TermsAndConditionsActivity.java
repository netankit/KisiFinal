package de.kisi.android.account;

import de.kisi.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class TermsAndConditionsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_and_conditions);
		WebView wv = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.loadUrl("http://www.getkisi.com/legal/terms/");
	}
}