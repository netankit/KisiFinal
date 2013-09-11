package com.electricimp.blinkup;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WifiSelectActivity extends Activity {
    private String setupToken;
    private String planID;

    private BlinkupController blinkup;

    private ListView networkListView;

    private List<NetworkItem> networkListStrings;
    private ArrayList<String> savedNetworks;
    private String preferenceFile;

    static final String SAVED_PASSWORD_PREFIX = "eimp:w:";
    static final String SAVED_NETWORKS_SETTING = "eimp:savedNetworks";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_wifi_select);

        blinkup = BlinkupController.getInstance();

        Bundle bundle = getIntent().getExtras();

        savedNetworks = new ArrayList<String>();

        setupToken = bundle.getString("setupToken");
        planID = bundle.getString("planID");
        preferenceFile = bundle.getString("preferenceFile");

        BlinkupController blinkup = BlinkupController.getInstance();

        networkListView = (ListView) findViewById(R.id.__bu_network_list);

        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.__bu_wifi_select_header,
                networkListView, false);
        networkListView.addHeaderView(header, null, false);

        TextView headerText = (TextView) findViewById(
                R.id.__bu_wifi_select_header);
        BlinkupController.setText(headerText, blinkup.stringIdChooseWiFiNetwork,
                R.string.__bu_choose_wifi_network);

        networkListStrings = new ArrayList<NetworkItem>();
        networkListView.setAdapter(new ArrayAdapter<NetworkItem>(this,
                R.layout.__bu_network_list_item, networkListStrings));
        networkListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        ListAdapter adapter = (ListAdapter) parent.getAdapter();
                        NetworkItem item = (NetworkItem) adapter
                                .getItem(position);
                        if (item == null) {
                            return;
                        }

                        switch (item.type) {
                        case CHANGE_NETWORK:
                            sendWirelessConfiguration(null);
                            break;
                        case CONNECT_USING_WPS:
                            connectUsingWPS();
                            break;
                        case CLEAR:
                            Intent intent = new Intent(WifiSelectActivity.this,
                                    ClearWifiActivity.class);
                            startActivityForResult(
                                    intent, BlinkupController.CLEAR_REQUEST_CODE);
                            break;
                        default:
                            sendWirelessConfiguration(item.label);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        String currentSSID = null;
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(
                    Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                if (wifiInfo.getSSID() != null) {
                    currentSSID = wifiInfo.getSSID().replaceAll("\"", "");
                }
            }
        } catch (Exception e) {
            Log.v(BlinkupController.TAG, "Error getting the current network");
            Log.v(BlinkupController.TAG, Log.getStackTraceString(e));
        }

        savedNetworks.clear();
        SharedPreferences pref = getSharedPreferences(
                preferenceFile, MODE_PRIVATE);
        String savedNetworksJSONStr = pref.getString(
                SAVED_NETWORKS_SETTING, "");
        try {
            JSONArray savedNetworksJSON = new JSONArray(savedNetworksJSONStr);
            for (int i = 0; i < savedNetworksJSON.length(); ++i) {
                savedNetworks.add(savedNetworksJSON.getString(i));
            }
        } catch (JSONException e) {
            Log.v(BlinkupController.TAG,
                    "Error parsing saved networks JSON string: " + e);
        }

        networkListStrings.clear();
        if (currentSSID != null) {
            networkListStrings.add(new NetworkItem(NetworkItem.Type.NETWORK,
                    currentSSID));
        }
        for (String s : savedNetworks) {
            if (s.equals(currentSSID)) {
                continue;
            }
            networkListStrings
                    .add(new NetworkItem(NetworkItem.Type.NETWORK, s));
        }

        Resources res = getResources();
        networkListStrings.add(new NetworkItem(NetworkItem.Type.CHANGE_NETWORK,
                res.getString(R.string.__bu_change_network)));
        networkListStrings.add(new NetworkItem(
                NetworkItem.Type.CONNECT_USING_WPS, res
                        .getString(R.string.__bu_connect_using_wps)));

        String clearSettingsText = blinkup.stringIdClearDeviceSettings;
        if (clearSettingsText == null) {
            clearSettingsText = getString(R.string.__bu_clear_device_settings);
        }
        networkListStrings.add(new NetworkItem(NetworkItem.Type.CLEAR,
                clearSettingsText));

        networkListView.invalidateViews();
        super.onResume();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == BlinkupController.WIFI_REQUEST_CODE ||
            requestCode == BlinkupController.WPS_REQUEST_CODE) {
            if (blinkup.intentBlinkupComplete != null) {
                setResult(RESULT_OK);
                finish();
            }
        }
        if (requestCode == BlinkupController.CLEAR_REQUEST_CODE) {
           if (blinkup.intentClearComplete != null) {
               setResult(RESULT_OK);
               finish();
           }
        }
    }

    private void sendWirelessConfiguration(String ssid) {
        Intent intent = new Intent();
        intent.putExtra("token", setupToken);
        if (ssid != null) {
            intent.putExtra("ssid", ssid);
        }
        intent.putExtra("siteid", planID);

        intent.putStringArrayListExtra("savedNetworks", savedNetworks);
        intent.putExtra("preferenceFile", preferenceFile);
        intent.setClass(this, WifiActivity.class);
        startActivityForResult(intent, BlinkupController.WIFI_REQUEST_CODE);
    }

    private void connectUsingWPS() {
        Intent myIntent = new Intent();
        myIntent.putExtra("token", setupToken);
        myIntent.putExtra("siteid", planID);

        myIntent.setClass(this, WPSActivity.class);
        startActivityForResult(myIntent, BlinkupController.WPS_REQUEST_CODE);
    }

    private static class NetworkItem {
        public enum Type {
            NETWORK,
            CHANGE_NETWORK,
            CONNECT_USING_WPS,
            CLEAR
        }

        public Type type;
        public String label;

        public NetworkItem(Type type, String label) {
            this.type = type;
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }
}