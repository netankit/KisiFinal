package de.kisi.android;

import de.kisi.android.api.KisiAPI;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// from http://stackoverflow.com/questions/14423981/android-webview-display-only-some-part-of-website

//TODO: tk: this needs to become a proper part of the app. no webviews.
//...though this hack at the bottom is awesome.
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
			url += "auth_token=" + KisiAPI.getInstance().getUser().getAuthentication_token();
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
