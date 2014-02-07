package com.electricimp.blinkup;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WifiSelectActivity extends BaseWifiSelectActivity {
    private ListView networkListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_wifi_select);

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

        addFooter(networkListView, R.string.__bu_logged_in_as, R.dimen.__bu_padding);

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
        String currentSSID = BlinkupController.getCurrentWifiSSID(this);

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

        BlinkupController blinkup = BlinkupController.getInstance();
        String changeNetwork = (blinkup.stringIdChangeNetwork != null) ?
                blinkup.stringIdChangeNetwork :
                getString(R.string.__bu_change_network);
        networkListStrings.add(new NetworkItem(
                NetworkItem.Type.CHANGE_NETWORK, changeNetwork));
        String connectUsingWps = (blinkup.stringIdConnectUsingWps != null) ?
                blinkup.stringIdConnectUsingWps :
                getString(R.string.__bu_connect_using_wps);
        networkListStrings.add(new NetworkItem(
                NetworkItem.Type.CONNECT_USING_WPS, connectUsingWps));

        String clearSettingsText = blinkup.stringIdClearDeviceSettings;
        if (clearSettingsText == null) {
            clearSettingsText = getString(R.string.__bu_clear_device_settings);
        }
        networkListStrings.add(new NetworkItem(NetworkItem.Type.CLEAR,
                clearSettingsText));

        networkListView.invalidateViews();
        super.onResume();
    }
}