package de.kisi.android;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// from http://stackoverflow.com/questions/14423981/android-webview-display-only-some-part-of-website

public class WebClient extends WebViewClient {

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// TODO Auto-generated method stub
		//view.setVisibility(View.INVISIBLE);		
		super.onPageStarted(view, url, favicon);

	}



	@Override
	// Append token to url. This method is not called for requests using the POST "method"!
	// possible workaround: https://code.google.com/p/android/issues/detail?id=9122#c3
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if ( url.indexOf("https://kisi.de/") == 0 ) {
			url += url.contains("?") ? "&" : "?";
			url += "auth_token=" + KisiApi.getAuthToken();
		}
		view.loadUrl(url);
		return true;
	}
	
	

	@Override
	public void onPageFinished(WebView view, String url) {
		//view.scrollTo(0, 130);
		view.loadUrl("javascript: document.getElementsByTagName('header')[0].style.display = 'none';");
		view.loadUrl("javascript: document.body.style.paddingTop = 0;");
		view.loadUrl("javascript: document.getElementById('footer').style.display = 'none';");
		//view.setVisibility(View.VISIBLE);
	}
}
