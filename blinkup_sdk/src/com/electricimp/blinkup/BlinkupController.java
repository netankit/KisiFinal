package com.electricimp.blinkup;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class BlinkupController {
    /**
     * Show checkbox to enable legacy mode.
     *
     * A few older Imp cards need this mode to BlinkUp the first time. Once
     * they're online (indicated by a green light) they'll be automatically
     * updated to the latest version of the imp OS and won't need Legacy
     * BlinkUp Mode again.
     */
    public boolean showLegacyMode = false;

    /** Resource Id for the custom string to choose wifi network. */
    public String stringIdChooseWiFiNetwork = null;
    /** Resource Id for the custom string to clear device settings. */
    public String stringIdClearDeviceSettings = null;
    /** Resource Id for the custom string for the Send BlinkUp button.  */
    public String stringIdSendBlinkUp = null;
    /** Resource Id for the custom string for the BlinkUp description. */
    public String stringIdBlinkUpDesc = null;

    /** Resource Id for the image to activate the interstitial page. */
    public int drawableIdInterstitial = 0;

    /**
     * Intent to launch your activity after flashing the wifi settings via
     * BlinkUp. You need to set this and call
     * {@link #handleActivityResult(Activity, int, int, Intent) handleActivityResult}
     * from the {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     * of your activity.
     */
    public Intent intentBlinkupComplete = null;
    /**
     * Intent to launch your activity after clearing the wifi settings via
     * BlinkUp. You need to set this and call
     * {@link #handleActivityResult(Activity, int, int, Intent) handleActivityResult}
     * from the {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     * of your activity.
     */
    public Intent intentClearComplete = null;

    /**
     * Get a BlinkupController instance.
     *
     * @return BlinkupController
     */
    public static BlinkupController getInstance() {
        return getInstanceWithBaseUrl(DEFAULT_BASE_URL);
    }

    /**
     * Get a BlinkupController instance pointing to a specific base url.
     *
     * @return BlinkupController
     */
    public static BlinkupController getInstanceWithBaseUrl(String baseUrl) {
        if (instance == null) {
            instance = new BlinkupController(baseUrl);
        }
        return instance;
    }

    /**
     * Set planID for all requests. Most developers won't need to reuse existing
     * planIDs. Please contact support if you have questions.
     */
    public void setPlanID(String planID) {
        impController.planID = planID;
    }

    /**
     * Check if there is a recent blinkup for resending.
     *
     * @return true if there is a recent flash
     */
    public boolean canResendLastBlinkUp() {
        return (lastPacket != null);
    }

    /**
     * Resend the most recent blinkup. Does nothing if no recent flash.
     *
     * @param context Context to launch blinkup
     */
    public void resendLastBlinkUp(Context context) {
        if (!canResendLastBlinkUp()) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("bitStream", lastPacket.toString());
        intent.putExtra(BlinkupPacket.FIELD_MODE, lastMode);
        intent.setClass(context, BlinkupGLActivity.class);
        context.startActivity(intent);
    }

    /**
     * Clear cached and persistent data (saved passwords, most recent blinkup,
     * etc)
     *
     * @param context Context to clear data
     */
    public void clearSavedData(Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                PREFERENCE_FILE_DEFAULT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

        lastPacket = null;
        lastMode = null;

        impController.setupToken = null;
        impController.planID = null;
        impController.apiKey = null;
    }

    /**
     * Bring up the wifi select interface to configure an Imp.
     *
     * @param activity Originating activity
     * @param apiKey Your api key
     */
    public void selectWifiAndSetupDevice(Activity activity, String apiKey) {
        if (impController.setupToken == null) {
            Handler handler = new LaunchWifiSelectHandler(activity);
            impController.acquireSetupToken(apiKey, handler);
        } else {
            selectWifiAndSetupDevice(activity);
        }
    }

    /**
     * Show BlinkUp directly without going through the wifi select interface.
     *
     * @param activity Originating activity
     * @param ssid SSID for flashing
     * @param password Password for flashing
     * @param apiKey Your api key
     */
    public void setupDevice(
            Activity activity, String ssid, String password, String apiKey) {
        if (impController.setupToken == null) {
            Handler handler = new LaunchWifiFlashHandler(
                    activity, ssid, password);
            impController.acquireSetupToken(apiKey, handler);
        } else {
            setupDeviceInternal(activity, ssid, password);
        }
    }

    /**
     * Show BlinkUp directly without going through the wifi select interface.
     *
     * @param activity Originating activity
     * @param wpsPin WPS pin for flashing
     * @param apiKey Your api key
     */
    public void setupDevice(
            Activity activity, String wpsPin, String apiKey) {
        if (impController.setupToken == null) {
            Handler handler = new LaunchWifiFlashHandler(activity, wpsPin);
            impController.acquireSetupToken(apiKey, handler);
        } else {
            setupDeviceInternal(activity, wpsPin);
        }
    }

    /**
     * Show BlinkUp directly to clear device settings on the Imp.
     *
     * @param activity Originating activity
     */
    public void clearDevice(Activity activity) {
        Intent intent = new Intent();
        intent.putExtra("mode", BlinkupPacket.MODE_CLEAR_WIFI);
        addBlinkupIntentFields(activity, intent);
        activity.startActivityForResult(intent, DIRECT_CLEAR_REQUEST_CODE);
    }

    /**
     * Call this from {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     * of your activity if you have specified a custom complete activity via
     * {@link #intentBlinkupComplete intentBlinkupComplete} or
     * {@link #intentClearComplete intentClearComplete}
     *
     * @param activity Your activity
     * @param requestCode Request code from {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     * @param resultCode Result code from {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     * @param data Intent from {@link Activity#onActivityResult(int, int, Intent) onActivityResult}
     */
    public void handleActivityResult(
            Activity activity,
            int requestCode, int resultCode, Intent data) {
        BlinkupController blinkup = BlinkupController.getInstance();
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (blinkup.lastMode == null) {
            return;
        }
        if (blinkup.lastMode.equals(BlinkupPacket.MODE_CLEAR_WIFI)) {
            if (blinkup.intentClearComplete != null) {
                activity.startActivity(blinkup.intentClearComplete);
            }
        } else {
            if (blinkup.intentBlinkupComplete != null) {
                activity.startActivity(blinkup.intentBlinkupComplete);
            }
        }
    }

    /**
     * Fetch the agent url and verified date after BlinkUp.
     *
     * @param callback Callback to handle success, error and timeout.
     */
    public void getTokenStatus(TokenStatusCallback callback) {
        impController.getTokenStatus(new TokenStatusHandler(callback));
    }

    /**
     * Interface to fetch the agent url and verified date after BlinkUp.
     */
    public interface TokenStatusCallback {
        public void onSuccess(Date verifiedDate, String agentUrl);
        public void onError(String errorMsg);
        public void onTimeout();
    }

    ///// Package private
    static final String TAG = "BlinkUp";

    static final int NETWORK_LIST_REQUEST_CODE = 0;
    static final int WIFI_REQUEST_CODE = 1;
    static final int WPS_REQUEST_CODE = 2;
    static final int CLEAR_REQUEST_CODE = 3;
    static final int BLINKUP_REQUEST_CODE = 4;
    static final int DIRECT_BLINKUP_REQUEST_CODE = 5;
    static final int DIRECT_CLEAR_REQUEST_CODE = 6;

    void selectWifiAndSetupDeviceWithSetupToken(
            Activity activity, String setupToken, String planID) {
        impController.setupToken = setupToken;
        impController.planID = planID;
        selectWifiAndSetupDevice(activity);
    }

    void savePacketForResend(BlinkupPacket packet, String mode) {
        lastPacket = packet;
        lastMode = mode;
    }

    static void setText(TextView view, String str, int defaultResId) {
        if (str != null) {
            view.setText(str);
            return;
        }
        view.setText(defaultResId);
    }

    void addBlinkupIntentFields(Context context, Intent intent) {
        if (!intent.getBooleanExtra(BlinkupPacket.FIELD_SLOW, false)) {
            intent.putExtra(BlinkupPacket.FIELD_TRILEVEL, true);
        }

        if (drawableIdInterstitial > 0) {
            intent.setClass(context, InterstitialActivity.class);
        } else {
            intent.setClass(context, BlinkupGLActivity.class);
        }
    }

    ///// Private
    private ImpController impController;

    private BlinkupPacket lastPacket = null;
    private String lastMode = null;

    private static final String DEFAULT_BASE_URL
        = "https://api.electricimp.com/v1";
    private static final String PREFERENCE_FILE_DEFAULT = "eimpPreferences";
    private String preferenceFile = PREFERENCE_FILE_DEFAULT;

    private static BlinkupController instance = null;
    private BlinkupController(String baseUrl) {
        impController = new ImpController(baseUrl);
    }

    private static class LaunchWifiSelectHandler extends Handler {
        private WeakReference<Activity> activity;

        public LaunchWifiSelectHandler(Activity activity) {
            this.activity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (activity.get() == null) {
                return;
            }
            BlinkupController blinkup = getInstance();
            blinkup.selectWifiAndSetupDevice(activity.get());
        }
    }

    private static class LaunchWifiFlashHandler extends Handler {
        private WeakReference<Activity> activity;
        private String ssid = null;
        private String password = null;
        private String wpsPin = null;

        public LaunchWifiFlashHandler(
                Activity activity, String ssid, String password) {
            this.activity = new WeakReference<Activity>(activity);
            this.ssid = ssid;
            this.password = password;
        }

        public LaunchWifiFlashHandler(Activity activity, String wpsPin) {
            this.activity = new WeakReference<Activity>(activity);
            this.wpsPin = wpsPin;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            if (activity.get() == null) {
                return;
            }
            BlinkupController blinkup = getInstance();
            if (wpsPin != null) {
                blinkup.setupDeviceInternal(activity.get(), wpsPin);
            } else {
                blinkup.setupDeviceInternal(activity.get(), ssid, password);
            }
        }
    }

    private static class TokenStatusHandler extends Handler {
        private TokenStatusCallback callback;
        private SimpleDateFormat dateFormat;

        public TokenStatusHandler(TokenStatusCallback callback) {
            this.callback = callback;
            dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        }

        @Override
        public void handleMessage(Message msg) {
            if (callback == null) {
                return;
            }
            switch (msg.arg1) {
            case BlinkupConstants.RESPONSE_SUCCESS:
                handleSuccess((JSONObject) msg.obj);
                break;
            case BlinkupConstants.RESPONSE_ERROR:
                callback.onError((String) msg.obj);
                break;
            case BlinkupConstants.RESPONSE_TIMEOUT:
                callback.onTimeout();
                break;
            }
        }

        private void handleSuccess(JSONObject json) {
            try {
                String dateStr = json.getString("claimed_at");
                dateStr = dateStr.replace("Z", "+0:00");
                Date claimedAt = dateFormat.parse(dateStr);

                String agentUrl = json.getString("agent_url");

                callback.onSuccess(claimedAt, agentUrl);
            } catch (JSONException e) {
                callback.onError(e.getMessage());
            } catch (ParseException e) {
                callback.onError(e.getMessage());
            }
        }
    }

    private void setupDeviceInternal(
            Activity activity, String ssid, String password) {
        if (ssid == null || ssid.length() == 0) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("mode", "wifi");
        intent.putExtra("ssid", ssid);
        intent.putExtra("pwd", password);
        intent.putExtra("token", impController.setupToken);
        intent.putExtra("siteid", impController.planID);
        addBlinkupIntentFields(activity, intent);

        activity.startActivityForResult(intent, DIRECT_BLINKUP_REQUEST_CODE);
    }

    private void setupDeviceInternal(Activity activity, String wpsPin) {
        Intent intent = new Intent();
        intent.putExtra("mode", "wps");
        intent.putExtra("pin", wpsPin);
        intent.putExtra("token", impController.setupToken);
        intent.putExtra("siteid", impController.planID);
        addBlinkupIntentFields(activity, intent);
        activity.startActivityForResult(intent, DIRECT_BLINKUP_REQUEST_CODE);
    }

    private void selectWifiAndSetupDevice(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, WifiSelectActivity.class);

        intent.putExtra("setupToken", impController.setupToken);
        intent.putExtra("planID", impController.planID);
        intent.putExtra("preferenceFile", preferenceFile);

        activity.startActivityForResult(intent, NETWORK_LIST_REQUEST_CODE);
    }
}