package de.kisi.android;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;


// base activity for the main activity 
// by extending this class the activity always checks if the location and the wifi is available  when it getting started
public class BaseActivity extends FragmentActivity{
	
	private	Dialog permissionDialog;
	
	@Override
	protected void onResume() {
		super.onResume();
		//check if there is already a dialog and if the user is already log in
		if(KisiAPI.getInstance().getUser() != null) {
			if(permissionDialog != null)
				permissionDialog.dismiss();
			checkForServices();
		}

	}
	
	private void checkForServices() {
		boolean locationEnabled = true;
		boolean wifiEnabled = true;
		boolean bluetoothEnabled = true;
		
		String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(!locationProviders.contains("gps") || !locationProviders.contains("network")) {
			locationEnabled = false;
		}
		
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifi.isWifiEnabled() && !BluetoothLEManager.getInstance().getServiceStatus()) {
			wifiEnabled = false;
		}
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!mBluetoothAdapter.isEnabled()) {
			bluetoothEnabled = false;
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
		

		permissionDialog = builder.create();
		permissionDialog.setCanceledOnTouchOutside(false);
		permissionDialog.show();
	}
}
