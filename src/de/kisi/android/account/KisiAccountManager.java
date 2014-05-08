package de.kisi.android.account;

import java.util.UUID;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.auth.GoogleAuthUtil;

import de.kisi.android.KisiApplication;

public class KisiAccountManager {
	private static KisiAccountManager instance;
	
	//TODO: put this into strings.xml
	private static final String ACCOUNT_TYPE =  "de.kisi";
	
	//TODO: necessary to keep this alive over the whole life time of the singleton?
	//why not fetch it dynamically whenever you need it? Any issues with this?
	private  AccountManager accManager;
	
	private KisiAccountManager(Context context) {
		accManager = AccountManager.get(context);
	}
	
	public static KisiAccountManager getInstance() {
		if(instance == null)
			instance = new KisiAccountManager(KisiApplication.getInstance());
		return instance;
	}

	public void deleteAccountByName(String name ) {
		Account availableAccounts[] = accManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);

		for(Account acc: availableAccounts) {
			if(acc.name.equals(name)) {
				accManager.removeAccount(acc, null, null);
				return;
			}
		}
		return;
	}	
	
	public String getDeviceUUID(String loginEmail) {
		//possible solutions:
		//phone number
		//String phoneNumber = this.getPhoneNumber();
		//Google Account name
		String googleAccount = this.getGoogleAccount();
		
		//WiFi MAC address
		//String wifiMac = this.getWifiMacAddress();
		
		//IMEI
		//String imei = this.getIMEI();
		if (googleAccount == null) {
			return null;
		} else {
			UUID uuid = UUID.nameUUIDFromBytes(googleAccount.getBytes());
			return uuid.toString();
		}
	}
	
	//can return null
	//needs permission in manifest:
	//<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
//	private String getPhoneNumber() {
//		TelephonyManager telephonyManager = (TelephonyManager)KisiApplication.getApplicationInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		String phoneNumber = telephonyManager.getLine1Number();
//		return phoneNumber;
//	}
	
	//can return null
	private String getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager)KisiApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getDeviceId();
		return phoneNumber;
	}
	
	//can be null, but only if no Google account is associated with the device
	private String getGoogleAccount() {
		Account[] accounts = accManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    String[] names = new String[accounts.length];
	    for (int i = 0; i < names.length; i++) {
	        names[i] = accounts[i].name;
	    }
	    return names.length > 0 ? names[0] : null;
	}
	
	//can be null if Wifi is turned off (not true for Moto X, as always visible)
	//we could actually turn on the Wifi progammatically, but Wifi MAC address is not great anyway
	private String getWifiMacAddress() {
		WifiManager wifiMgr = (WifiManager) KisiApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
		return wifiMgr.getConnectionInfo().getMacAddress();
	}
}
