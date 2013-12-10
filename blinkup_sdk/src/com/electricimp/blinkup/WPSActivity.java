package com.electricimp.blinkup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class WPSActivity extends PreBlinkUpActivity {
    private String token;
    private String siteids;
    private String apiKey;

    private EditText wpsPinView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_wps);

        init();

        wpsPinView = (EditText) findViewById(R.id.__bu_wps_pin_optional);
        BlinkupController.setHint(wpsPinView, blinkup.stringIdWpsPinHint,
                R.string.__bu_wps_pin);
        wpsPinView.setTypeface(Typeface.DEFAULT);

        TextView wpsInfo = (TextView) findViewById(R.id.__bu_wps_info);
        BlinkupController.setText(wpsInfo, blinkup.stringIdWpsInfo,
                R.string.__bu_wps_info);

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
        siteids = bundle.getString("siteid");
        apiKey = bundle.getString("apiKey");
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
        String pin = wpsPinView.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("mode", "wps");
        intent.putExtra("pin", pin);
        intent.putExtra("token", token);
        intent.putExtra("siteid", siteids);
        intent.putExtra("apiKey", apiKey);

        return intent;
    }
}