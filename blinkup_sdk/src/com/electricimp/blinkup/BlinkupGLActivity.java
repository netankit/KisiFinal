package com.electricimp.blinkup;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class BlinkupGLActivity extends Activity {
    private BlinkupSurfaceView mGLView;

    private View mCountdownPanel;
    private TextView mCountdownView;

    private BlinkupHandler mHandler;

    private int countdownCounter = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.__bu_blinkup);

        mCountdownPanel = findViewById(R.id.__bu_countdown_panel);
        mCountdownView = (TextView) findViewById(R.id.__bu_countdown);
        mCountdownView.setText(String.valueOf(countdownCounter));

        // Set screen brightness to 100%
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f;
        getWindow().setAttributes(params);

        // Create BlinkupPacket from intent
        BlinkupPacket packet = BlinkupPacket.createFromIntent(getIntent());
        BlinkupController blinkup = BlinkupController.getInstance();
        String mode = getIntent().getExtras().getString("mode");
        blinkup.savePacketForResend(packet, mode);
        if (packet == null) {
            finish();
        }

        // Insert BlinkupSurfaceView behind the countdown panel
        FrameLayout container = (FrameLayout) findViewById(R.id.__bu_container);
        mGLView = new BlinkupSurfaceView(this, packet);
        container.addView(mGLView, 0);

        // Start counting down
        mHandler = new BlinkupHandler(this);
        mHandler.sendEmptyMessageDelayed(BlinkupHandler.UPDATE_COUNTDOWN,
                DateUtils.SECOND_IN_MILLIS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private static class BlinkupHandler extends Handler {
        public static final int UPDATE_COUNTDOWN = 0;
        public static final int START_TRANSMITTING = 1;

        private WeakReference<BlinkupGLActivity> mActivity;

        public BlinkupHandler(BlinkupGLActivity activity) {
            mActivity = new WeakReference<BlinkupGLActivity>(activity);
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
                activity.mGLView.startTransmitting();
                break;
            }
        }
    }
}

class BlinkupSurfaceView extends GLSurfaceView {
    private BlinkupRenderer mRenderer;

    public BlinkupSurfaceView(Activity activity, BlinkupPacket packet) {
        super(activity);
        mRenderer = new BlinkupRenderer(activity, packet);
        setRenderer(mRenderer);
    }

    public void startTransmitting() {
        mRenderer.startTransmitting();
    }
}