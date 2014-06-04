package de.kisi.android.api;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import de.kisi.android.BaseActivity;
import de.kisi.android.R;

public class GeofencingEnabler {
                                                                                
	public void enableGeofencing(ContentResolver contentResolver, 
			BaseActivity activity) {
		String locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!("gps".equals(locationProviders))) {                                 // !
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater adbInflater = LayoutInflater.from(activity);
		View notificationLayout = adbInflater.inflate(R.layout.warning, null);
		builder.setView(notificationLayout);
        builder.setTitle("Geofencing Not Available");
		builder.setMessage("Please, enable Wi-Fi or Network for location detection manually");
		builder.setNegativeButton("Ok",  new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			} 
		});
		
		Dialog permissionDialog;
		permissionDialog = builder.create();
		permissionDialog.setCanceledOnTouchOutside(false);
		permissionDialog.show();
	}
}
