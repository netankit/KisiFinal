package com.electricimp.blinkup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ClearWifiActivity extends PreBlinkUpActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_clear_wifi);

        init();

        TextView blinkupDesc = (TextView) findViewById(R.id.__bu_blinkup_desc);
        BlinkupController.setText(blinkupDesc, blinkup.stringIdBlinkUpDesc,
                R.string.__bu_blinkup_desc);

        TextView headerText = (TextView) findViewById(
                R.id.__bu_clear_wifi_header);
        BlinkupController.setText(headerText, blinkup.stringIdClearDeviceSettings,
                R.string.__bu_clear_device_settings);

        BlinkupController.setText(blinkupButton, blinkup.stringIdClearWireless,
                R.string.__bu_clear_wireless);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == BlinkupController.BLINKUP_REQUEST_CODE) {
            if (blinkup.intentClearComplete != null) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public Intent createSendBlinkupIntent() {
        Intent intent = new Intent();
        intent.putExtra("mode", BlinkupPacket.MODE_CLEAR_WIFI);
        return intent;
    }
}