package com.electricimp.blinkup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public abstract class PreBlinkUpActivity extends Activity {
    private static final int LEGACY_MODE_DIALOG = 1000;

    protected BlinkupController blinkup;

    protected Button blinkupButton;
    protected CheckBox legacyModeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blinkup = BlinkupController.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blinkupButton.setEnabled(true);
    }

    protected void init() {
        blinkupButton = (Button) findViewById(R.id.__bu_blinkup_button);

        legacyModeCheckBox = (CheckBox) findViewById(
                R.id.__bu_legacy_mode_checkbox);
        BlinkupController.setText(legacyModeCheckBox,
                blinkup.stringIdLegacyMode, R.string.__bu_legacy_mode);

        TextView blinkupDesc = (TextView) findViewById(R.id.__bu_blinkup_desc);
        if (blinkupDesc != null) {
            BlinkupController.setText(blinkupDesc, blinkup.stringIdBlinkUpDesc,
                  R.string.__bu_blinkup_desc);
        }

        if (blinkup.drawableIdInterstitial > 0) {
            BlinkupController.setText(blinkupButton, blinkup.stringIdNext,
                    R.string.__bu_next);
        } else {
            BlinkupController.setText(blinkupButton, blinkup.stringIdSendBlinkUp,
                    R.string.__bu_send_blinkup);
        }

        if (blinkup.showLegacyMode) {
            View legacyMode = findViewById(R.id.__bu_legacy_mode);
            legacyMode.setVisibility(View.VISIBLE);
        }
    }

    protected abstract Intent createSendBlinkupIntent();

    public void sendBlinkup(View view) {
        blinkupButton.setEnabled(false);
        Intent intent = createSendBlinkupIntent();
        if (legacyModeCheckBox.isChecked()) {
            intent.putExtra(BlinkupPacket.FIELD_SLOW, true);
        }
        blinkup.addBlinkupIntentFields(this, intent);
        startActivityForResult(intent, BlinkupController.BLINKUP_REQUEST_CODE);
    }

    @SuppressWarnings("deprecation")
    public void legacyModeLink(View v) {
        showDialog(LEGACY_MODE_DIALOG);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (id == LEGACY_MODE_DIALOG) {
            builder.setTitle(BlinkupController.getCustomStringOrDefault(this,
                    blinkup.stringIdLegacyMode, R.string.__bu_legacy_mode));
            builder.setMessage(BlinkupController.getCustomStringOrDefault(this,
                    blinkup.stringIdLegacyModeDesc,
                    R.string.__bu_legacy_mode_desc));
            builder.setNeutralButton(BlinkupController.getCustomStringOrDefault(
                    this, blinkup.stringIdOk, R.string.__bu_ok),
                    new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        return builder.create();
    }
}