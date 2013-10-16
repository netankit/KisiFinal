package com.electricimp.blinkup;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class BlinkupGLActivity extends Activity {
    private static final int DIALOG_LOW_FRAME_RATE = 0;

    private BlinkupSurfaceView mGLView;

    private View mCountdownPanel;
    private TextView mCountdownView;

    private BlinkupHandler mHandler;
    private PowerManager.WakeLock mWakeLock;

    private int countdownCounter = 3;

    private SetupTokenHandler setupTokenHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.__bu_blinkup);

        mCountdownPanel = findViewById(R.id.__bu_countdown_panel);
        mCountdownView = (TextView) findViewById(R.id.__bu_countdown);
        mCountdownView.setText(String.valueOf(countdownCounter));

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "BrightWhileBlinking");

        // Set screen brightness to 100%
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f;
        getWindow().setAttributes(params);

        // Insert BlinkupSurfaceView behind the countdown panel
        FrameLayout container = (FrameLayout) findViewById(R.id.__bu_container);
        float maxSize = (float) getResources().getDimensionPixelSize(
                R.dimen.__bu_blinkup_max_size);
        mGLView = new BlinkupSurfaceView(this, maxSize);
        @SuppressWarnings("deprecation")
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
                Gravity.CENTER);
        container.addView(mGLView, 0, layoutParams);

        String apiKey = getIntent().getExtras().getString("apiKey");
        if (apiKey != null) {
            BlinkupController blinkup = BlinkupController.getInstance();
            setupTokenHandler = new SetupTokenHandler();
            blinkup.acquireSetupToken(this, apiKey, setupTokenHandler);
        } else {
            initBlinkUp(getIntent());
        }
    }

    private void initBlinkUp(Intent intent) {
        // Create BlinkupPacket from intent
        BlinkupPacket packet = BlinkupPacket.createFromIntent(intent);
        BlinkupController blinkup = BlinkupController.getInstance();
        String mode = intent.getExtras().getString("mode");
        blinkup.saveLastMode(mode);
        if (packet == null) {
            finish();
        }

        mHandler = new BlinkupHandler(this, packet);
        if (blinkup.shouldCheckFrameRate(this)) {
            mHandler.sendEmptyMessageDelayed(
                BlinkupHandler.MEASURE_FRAME_RATE,
                DateUtils.SECOND_IN_MILLIS);
        } else {
            mHandler.sendEmptyMessageDelayed(
                    BlinkupHandler.UPDATE_COUNTDOWN,
                    DateUtils.SECOND_IN_MILLIS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();

        mWakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }

    private void updateCountdown() {
        countdownCounter--;
        mCountdownView.setText(String.valueOf(countdownCounter));
        if (countdownCounter > 0) {
            mHandler.sendEmptyMessageDelayed(BlinkupHandler.UPDATE_COUNTDOWN,
                    DateUtils.SECOND_IN_MILLIS);
        } else {
            mCountdownPanel.setVisibility(View.GONE);
            mHandler.sendEmptyMessageDelayed(BlinkupHandler.START_TRANSMITTING,
                    250);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id != DIALOG_LOW_FRAME_RATE) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.__bu_low_frame_rate_title);
        builder.setMessage(R.string.__bu_low_frame_rate_desc);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.__bu_low_frame_rate_go_to_settings,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                goToSettings();
                finish();
            }
        });
        builder.setNegativeButton(R.string.__bu_low_frame_rate_proceed_anyway,
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

    private static class BlinkupHandler extends Handler {
        public static final int UPDATE_COUNTDOWN = 0;
        public static final int START_TRANSMITTING = 1;
        public static final int MEASURE_FRAME_RATE = 2;

        private WeakReference<BlinkupGLActivity> mActivity;
        private BlinkupPacket mPacket;

        public BlinkupHandler(
                BlinkupGLActivity activity,
                BlinkupPacket packet) {
            mActivity = new WeakReference<BlinkupGLActivity>(activity);
            mPacket = packet;
        }

        @Override
        public void handleMessage(Message msg) {
            BlinkupGLActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
            case UPDATE_COUNTDOWN:
                activity.updateCountdown();
                break;
            case START_TRANSMITTING:
                activity.mGLView.startTransmitting(mPacket);
                break;
            case MEASURE_FRAME_RATE:
                float framerate = activity.mGLView.getFrameRate();
                BlinkupController blinkup = BlinkupController.getInstance();
                if (blinkup.isFrameRateTooLow(framerate)) {
                    activity.showDialog(DIALOG_LOW_FRAME_RATE);
                } else {
                    sendEmptyMessage(UPDATE_COUNTDOWN);
                }
                break;
            }
        }
    }

    private class SetupTokenHandler
            implements BlinkupController.SetupTokenHandler {
        @Override
        public void onSuccess(String setupToken) {
            Intent intent = new Intent();
            intent.replaceExtras(getIntent().getExtras());
            intent.putExtra("token", setupToken);
            initBlinkUp(intent);
        }
    }
}
