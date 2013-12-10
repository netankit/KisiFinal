package com.electricimp.blinkup;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class WifiActivity extends PreBlinkUpActivity {
    private String token;
    private String siteids;
    private String apiKey;

    private String oldSSID;

    private EditText ssidView;
    private EditText passwordView;
    private CheckBox rememberCheckBox;

    private TextWatcher ssidWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            blinkupButton.setEnabled(s.length() > 0);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // Not used
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            // Not used
        }
    };

    private SharedPreferences sharedPreferences;
    private MCrypt mcrypt = new MCrypt(null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_wifi);

        init();

        String filename = getIntent().getExtras().getString("preferenceFile");
        sharedPreferences = getSharedPreferences(filename, MODE_PRIVATE);

        ssidView = (EditText) findViewById(R.id.__bu_wifi_ssid);
        BlinkupController.setHint(ssidView, blinkup.stringIdSsidHint,
                R.string.__bu_ssid);

        passwordView = (EditText) findViewById(R.id.__bu_wifi_password);
        BlinkupController.setHint(passwordView, blinkup.stringIdPasswordHint,
                R.string.__bu_password);

        rememberCheckBox = (CheckBox) findViewById(R.id.__bu_remember_password);
        BlinkupController.setText(rememberCheckBox,
                blinkup.stringIdRememberPassword,
                R.string.__bu_remember_password);

        passwordView.setTypeface(Typeface.DEFAULT);
        blinkupButton.setEnabled(false);
        ssidView.addTextChangedListener(ssidWatcher);

        TextView blinkupDesc = (TextView) findViewById(R.id.__bu_blinkup_desc);
        BlinkupController.setText(blinkupDesc, blinkup.stringIdBlinkUpDesc,
                R.string.__bu_blinkup_desc);

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
        siteids = bundle.getString("siteid");
        apiKey = bundle.getString("apiKey");

        String ssid = bundle.getString("ssid");
        oldSSID = ssid;
        if (ssid != null) {
            ssidView.setText(ssid);
            rememberCheckBox.setChecked(true);
        }

        String encrypted_pwd = sharedPreferences.getString(
                WifiSelectActivity.SAVED_PASSWORD_PREFIX + ssid, "");
        if (encrypted_pwd == null) {
            if (ssid != null) {
                passwordView.requestFocus();
            }
        } else {
            String pwd = decryptPassword(encrypted_pwd);
            passwordView.setText(pwd);
            getWindow().setSoftInputMode(
                      WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        blinkupButton.setEnabled(ssidView.getText().length() > 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSavedNetworks();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == BlinkupController.BLINKUP_REQUEST_CODE) {
            if (blinkup.intentBlinkupComplete != null) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    protected Intent createSendBlinkupIntent() {
        String ssid = ssidView.getText().toString();

        if (ssid.length() == 0) {
            return null;
        }

        String pwd = passwordView.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("mode", "wifi");
        intent.putExtra("ssid", ssid);
        intent.putExtra("pwd", pwd);
        intent.putExtra("token", token);
        intent.putExtra("siteid", siteids);
        intent.putExtra("apiKey", apiKey);

        return intent;
    }

    private void updateSavedNetworks() {
        String ssid = ssidView.getText().toString();
        String password = passwordView.getText().toString();

        ArrayList<String> savedNetworks = getIntent().getStringArrayListExtra(
                "savedNetworks");

        if (rememberCheckBox.isChecked()) {
            if (oldSSID != null && !oldSSID.equals(ssid)) {
                removeSavedNetwork(savedNetworks, oldSSID);
            }
            addSavedNetwork(savedNetworks, ssid, password);
        } else {
            removeSavedNetwork(savedNetworks, oldSSID);
        }
    }

    private void addSavedNetwork(List<String> savedNetworks, String ssid,
            String password) {
        if (ssid.length() == 0) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!savedNetworks.contains(ssid)) {
            savedNetworks.add(ssid.replaceAll("\"", ""));
            JSONArray savedNetworksJSON = new JSONArray(savedNetworks);
            editor.putString(WifiSelectActivity.SAVED_NETWORKS_SETTING,
                    savedNetworksJSON.toString());
        }
        editor.putString(WifiSelectActivity.SAVED_PASSWORD_PREFIX + ssid,
                encryptPassword(password));
        editor.commit();
    }

    private void removeSavedNetwork(List<String> savedNetworks, String ssid) {
        if (savedNetworks.contains(ssid)) {
            savedNetworks.remove(ssid);

            JSONArray savedNetworksJSON = new JSONArray(savedNetworks);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(WifiSelectActivity.SAVED_NETWORKS_SETTING,
                    savedNetworksJSON.toString());
            editor.remove(WifiSelectActivity.SAVED_PASSWORD_PREFIX + ssid);
            editor.commit();
        }
    }

    private String encryptPassword(String password) {
        if (password == null || password.length() == 0) {
            return null;
        }
        try {
            String encrypted = MCrypt.bytesToHex(mcrypt.encrypt(password));
            return encrypted;
        } catch (Exception e) {
            Log.e(BlinkupController.TAG, e.toString());
            return null;
        }
    }

    private String decryptPassword(String encrypted) {
        if (encrypted == null || encrypted.length() == 0) {
            return null;
        }
        try {
            String decrypted = mcrypt.decryptToString(encrypted);
            return decrypted;
        } catch (Exception e) {
            Log.e(BlinkupController.TAG, e.toString());
            return null;
        }
    }
}