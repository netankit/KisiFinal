package com.electricimp.blinkup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class BlinkupGLActivity extends BaseBlinkupGLActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BlinkupController blinkup = BlinkupController.getInstance();
        BlinkupController.setText(countdownDescView,
                blinkup.stringIdCountdownDesc, R.string.__bu_countdown_desc);

        startBlinkup();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id != DIALOG_LOW_FRAME_RATE) {
            return null;
        }
        BlinkupController blinkup = BlinkupController.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(BlinkupController.getCustomStringOrDefault(this,
                blinkup.stringIdLowFrameRateTitle,
                R.string.__bu_low_frame_rate_title));
        builder.setMessage(BlinkupController.getCustomStringOrDefault(this,
                blinkup.stringIdLowFrameRateDesc,
                R.string.__bu_low_frame_rate_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(BlinkupController.getCustomStringOrDefault(
                this,
                blinkup.stringIdLowFrameRateGoToSettings,
                R.string.__bu_low_frame_rate_go_to_settings),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                goToSettings();
                finish();
            }
        });
        builder.setNegativeButton(BlinkupController.getCustomStringOrDefault(
                this,
                blinkup.stringIdLowFrameRateProceedAnyway,
                R.string.__bu_low_frame_rate_proceed_anyway),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                updateCountdown();
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private void goToSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
}
