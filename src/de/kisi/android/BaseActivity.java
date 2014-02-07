package de.kisi.android;

import de.kisi.android.api.KisiAPI;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;


// base activity for the main activity 
// by extending this class the activity always checks if the location and the wifi is available  when it getting started
public class BaseActivity extends FragmentActivity{
	
	private Dialog mLocationAlertDialog;
	private Dialog mWifiAlertDialog;
	
	@Override
	protected void onResume() {
		super.onResume();
		//check if there is already a dialog and if the user is already log in
		if(KisiAPI.getInstance().getUser() != null) {
			if(mLocationAlertDialog == null) {
				checkForLocationService();
			}
			if(mWifiAlertDialog == null) {
				checkForWiFi();	
			}
		}

	}
	
	
	private void checkForLocationService() {
		// deprecated in API level 19, but the app should run on android v4.0+
		@SuppressWarnings("deprecation")
		String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(!locationProviders.contains("gps") || !locationProviders.contains("network")) {
		  // Build the alert dialog
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  builder.setTitle(R.string.location_not_available_title);
		  builder.setMessage(R.string.location_not_available_text);
		  builder.setPositiveButton(R.string.go_to_setting, new DialogInterface.OnClickListener() {
			  
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
			    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			    startActivity(intent);
			    dialog.dismiss();
			    mLocationAlertDialog = null;
			    }
			  });
		  builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			    mLocationAlertDialog = null;
			}});
		  mLocationAlertDialog = builder.create();
		  mLocationAlertDialog.setCanceledOnTouchOutside(false);
		  mLocationAlertDialog.show();
		}
	}
	
	private void checkForWiFi() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		SharedPreferences prefs = KisiApplication.getApplicationInstance().getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		boolean disableDialog = prefs.getBoolean(KisiAPI.getInstance().getUser().getId() + "-dontAskForWifi", false);
		if (!wifi.isWifiEnabled() && !disableDialog) {
			// Build the alert dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			LayoutInflater adbInflater = LayoutInflater.from(this);
		    View wifiLayout = adbInflater.inflate(R.layout.checkbox, null);
	        final CheckBox dontShowAgain = (CheckBox) wifiLayout.findViewById(R.id.skip);
			
	        builder.setView(wifiLayout);
	        builder.setTitle(R.string.wifi_not_available_title);
			builder.setMessage(R.string.wifi_not_available_text);
			builder.setPositiveButton(R.string.go_to_setting, new DialogInterface.OnClickListener() {
				
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
							startActivity(intent);
							dialog.dismiss();
							mWifiAlertDialog = null;
						}
					});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if(dontShowAgain.isChecked()) {
								SharedPreferences prefs = KisiApplication.getApplicationInstance().getSharedPreferences("userconfig", Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = prefs.edit();
								editor.putBoolean(KisiAPI.getInstance().getUser().getId() + "-dontAskForWifi" , true);
								editor.commit();
							}
							mWifiAlertDialog = null;
						}
					});
			mWifiAlertDialog = builder.create();
			mWifiAlertDialog.setCanceledOnTouchOutside(false);
			mWifiAlertDialog.show();
		}
	}
}
