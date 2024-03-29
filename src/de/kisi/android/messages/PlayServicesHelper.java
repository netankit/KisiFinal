package de.kisi.android.messages;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.messages.result.QBSubscriptionArrayResult;

import de.kisi.android.Config;
import de.kisi.android.KisiApplication;

/**
 * This class provides methods to use the Google Cloud Messaging service, 
 * which is needed by the QuickBlox library.
 * 
 * This class is based on a sample project from Quickblox website.
 * (http://quickblox.com/developers/SimpleSample-messages_users-android)
 */
public class PlayServicesHelper {

	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String TAG = "PlayServicesHelper";

	private GoogleCloudMessaging googleCloudMessaging;
	private Context context;
	private String regId;

	public PlayServicesHelper() {
		context = KisiApplication.getInstance();
		checkPlayService();
	}

	/**
	 * Check device for Play Services APK. 
	 * If check succeeds, proceed with GCM registration.
	 */
	private void checkPlayService() {
		if (checkPlayServices()) {
			googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
			regId = getRegistrationId();
			if (regId.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK.
	 */
	public boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Toast.makeText(context, "Please install Google Play Services",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context,
						"Could not initialize Google Play Services.",
						Toast.LENGTH_LONG).show();
				Log.i(TAG, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = KisiApplication.getInstance().getVersionCode();

		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		Log.d(TAG, "registering for pushnotifications.");
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (googleCloudMessaging == null) {
						googleCloudMessaging = GoogleCloudMessaging
								.getInstance(context);
					}
					regId = googleCloudMessaging.register(Config.SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					Handler h = new Handler(context.getMainLooper());
					h.post(new Runnable() {
						@Override
						public void run() {
							// Send the registration ID to Quickblox, so it
							// can use GCM to send messages to the app
							subscribeToPushNotifications(regId);
						}
					});
					
					// Persist the regID
					storeRegistrationId(regId);
				} catch (IOException ex) {
					Log.e(TAG, "Error while registering to GCM:" + ex.getMessage());
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG, msg + "\n");
			}
		}.execute(null, null, null);
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}

	/**
	 * Subscribe to Push Notifications
	 * @param regId registration ID
	 */
	private void subscribeToPushNotifications(String regId) {
		// Create push token with Registration Id for Android
		Log.d(TAG, "subscribing...");

		String deviceId = getDeviceID();
		
		QBMessages.subscribeToPushNotificationsTask(regId, deviceId,
				QBEnvironment.DEVELOPMENT, new QBCallbackImpl() {
					@Override
					public void onComplete(Result result) {
						if (result.isSuccess()) {
							Log.d(TAG, "subscribed");
						}
					}
				});
	}
	
	/**
	 * @return Returns a unique device id.
	 */
	public String getDeviceID(){
		String deviceId;

		final TelephonyManager mTelephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephony.getDeviceId() != null) {
			deviceId = mTelephony.getDeviceId(); // *** use for mobiles
		} else {
			deviceId = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.ANDROID_ID); // *** use for tablets
		}
		return deviceId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param regId registration ID
	 */
	private void storeRegistrationId(String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = KisiApplication.getInstance().getVersionCode();
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Unsubscribe from push notifications.
	 */
	public void unsubscribe() {
		try {
			QBMessages.getSubscriptions(new QBCallback() {
				@Override
				public void onComplete(Result arg0, Object arg1) {
				}

				@Override
				public void onComplete(Result result) {
					QBSubscriptionArrayResult subResult = (QBSubscriptionArrayResult) result;
					for (QBSubscription subscription : subResult.getSubscriptions()) {
						QBMessages.deleteSubscription(subscription, new QBCallback() {
							@Override
							public void onComplete(Result arg0,
									Object arg1) {
							}
							@Override
							public void onComplete(Result arg0) {
								Log.d("Logout", "Unsubscribed from QB messages");
							}
						});
					}
				}
			});
		} finally {
			final SharedPreferences prefs = getGCMPreferences();
			Log.i(TAG, "Deleting regId");
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(PROPERTY_REG_ID);
			editor.remove(PROPERTY_APP_VERSION);
			editor.commit();
		};
	}
}