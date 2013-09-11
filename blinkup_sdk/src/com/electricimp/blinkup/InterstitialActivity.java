package com.electricimp.blinkup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class InterstitialActivity extends Activity {
    private View blinkupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__bu_interstitial);

        blinkupButton = findViewById(R.id.__bu_blinkup_button);

        BlinkupController blinkup = BlinkupController.getInstance();
        ImageView image = (ImageView) findViewById(R.id.__bu_interstitial);
        image.setImageResource(blinkup.drawableIdInterstitial);
    }

    @Override
    protected void onResume() {
        super.onResume();
        blinkupButton.setEnabled(true);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        setResult(RESULT_OK);
        finish();
    }

    public void sendBlinkup(View view) {
        blinkupButton.setEnabled(false);
        Intent intent = new Intent(this, BlinkupGLActivity.class);
        intent.replaceExtras(getIntent().getExtras());
        startActivityForResult(intent, BlinkupController.BLINKUP_REQUEST_CODE);
    }
}