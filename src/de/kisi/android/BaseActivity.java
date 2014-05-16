package de.kisi.android;

import java.util.Date;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.VersionCheckCallback;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;


// base activity for the main activity 
// by extending this class the activity always checks if the location and the wifi is available  when it getting started
public class BaseActivity extends FragmentActivity implements VersionCheckCallback{
	
	private	Dialog permissionDialog;
	private Dialog mUpdateDialog;
	private static long TWO_WEEKS_IN_MS = 1209600000;
	
	@Override
	protected void onResume() {
		super.onResume();
		//check if there is already a dialog and if the user is already log in
		if(KisiAPI.getInstance().getUser() != null) {
			if(permissionDialog != null) 
				permissionDialog.dismiss();
			checkForServices();
			KisiAPI.getInstance().getLatestVerion(this);
		}
		
	}
	
	private void checkForServices() {
		boolean locationEnabled = true;
		boolean wifiEnabled = true;
		boolean bluetoothEnabled = true;
		
		SharedPreferences prefs = KisiApplication.getInstance().getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		boolean disableDialog = prefs.getBoolean(KisiAPI.getInstance().getUser().getId() + "-dontAskAgain", false);
		long datePerfMS = prefs.getLong(KisiAPI.getInstance().getUser().getId() + "-dontAskAgainDate", -1);
		//asked user again if more than 2 weeks have elapsed since the last checkbox
		if(datePerfMS != -1) {
			if(System.currentTimeMillis() - datePerfMS > TWO_WEEKS_IN_MS) {
				disableDialog = false;
			}
		}
		else {
			disableDialog = false;
		}
		
		if(disableDialog) {
			return; 
		}
		
		String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(!locationProviders.contains("gps") && !locationProviders.contains("network")) {
			locationEnabled = false;
		}
		
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifi.isWifiEnabled() && !BluetoothLEManager.getInstance().getServiceStatus()) {
			wifiEnabled = false;
		}
		
		//check if this device supports BLE
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				KisiApplication.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if(!mBluetoothAdapter.isEnabled()) {
				bluetoothEnabled = false;
			}
		}
		else{ // dont ask for bluetooth if device does not support BLE
			bluetoothEnabled = true;
		}
			
		//check if there is a need to enable anything
		if(locationEnabled && wifiEnabled && bluetoothEnabled) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater adbInflater = LayoutInflater.from(this);
	    View checkboxLayout = adbInflater.inflate(R.layout.checkbox, null);
        
	    final CheckBox locationCheckbox = (CheckBox) checkboxLayout.findViewById(R.id.location_checkbox);
	    final CheckBox wifiCheckbox = (CheckBox) checkboxLayout.findViewById(R.id.wifi_checkbox);
	    final CheckBox bluetoothCheckbox = (CheckBox) checkboxLayout.findViewById(R.id.bluetooth_checkbox);
	    
	    if(locationEnabled)
	    	locationCheckbox.setVisibility(View.GONE);
	    if(wifiEnabled)
	    	wifiCheckbox.setVisibility(View.GONE);
	    if(bluetoothEnabled)
	    	bluetoothCheckbox.setVisibility(View.GONE);
	    
        builder.setView(checkboxLayout);
        builder.setTitle(R.string.location_not_available_title);
		builder.setMessage(R.string.location_not_available_text);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(locationCheckbox.getVisibility() == View.VISIBLE && locationCheckbox.isChecked())  {
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						    startActivity(intent);
						}
						if(wifiCheckbox.getVisibility() == View.VISIBLE  && wifiCheckbox.isChecked()) {
							WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
							wifi.setWifiEnabled(true);
						}
						if(bluetoothCheckbox.getVisibility() == View.VISIBLE  && bluetoothCheckbox.isChecked()) {
							BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
							 mBluetoothAdapter.enable();
						}
						dialog.dismiss();
						dialog = null;
					}
				});
		
		builder.setNegativeButton(R.string.dontaskagain,  new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences prefs = KisiApplication.getInstance().getSharedPreferences("userconfig", Context.MODE_PRIVATE);
				Date date = new Date(System.currentTimeMillis());
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(KisiAPI.getInstance().getUser().getId() + "-dontAskAgain" , true);
				editor.putLong(KisiAPI.getInstance().getUser().getId() + "-dontAskAgainDate", date.getTime());
				editor.commit();
			} });
		

		permissionDialog = builder.create();
		permissionDialog.setCanceledOnTouchOutside(false);
		permissionDialog.show();
	}
	
	@Override
	protected void onStart() {
		if(KisiAPI.getInstance().getUser() != null) {
			GeofenceManager.getInstance().startLocationUpdate();
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if(KisiAPI.getInstance().getUser() != null) {
			GeofenceManager.getInstance().stopLocationUpdate();
		}
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		GeofenceManager.getInstance().stopLocationUpdate();
		super.onDestroy();
	}
	
	private void updateButton() {

		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setVisibility(View.VISIBLE);
		final String appPackageName = getPackageName();

		updateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			}

		});
	}


	@Override
	public void onVersionResult(String result) {
		if(mUpdateDialog == null){
			String versionName[] = null;
			try {
				versionName = KisiApplication.getInstance().getVersion().split("\\.");
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			if(result != null || "error".equals(result) ) {
				String newstVersion[] = result.split("\\.");
				for(int i = 0; i < versionName.length; i++) {
					//version in stats is older the current version 
					if(Integer.valueOf(versionName[i]) > Integer.valueOf(newstVersion[i]) ){
						break;
					}
					
					if(Integer.valueOf(versionName[i]) < Integer.valueOf(newstVersion[i]) ){
						updateButton();
					}
				}
			}
		}
	}
}
