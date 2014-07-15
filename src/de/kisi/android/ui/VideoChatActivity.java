package de.kisi.android.ui;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.OpponentGlSurfaceView;

import org.jivesoftware.smack.XMPPException;

import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.listeners.OnCameraViewListener;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.module.videochat.model.utils.Debugger;
import com.quickblox.module.videochat.views.CameraView;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.QuickBloxApi;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class VideoChatActivity extends Activity {

    private CameraView cameraView;
    private OpponentGlSurfaceView opponentView;
    private ProgressBar opponentImageLoadingPb;
    private VideoChatConfig videoChatConfig;
    private Place p;
    private ListView mLockList;
    private LockListAdapter mLockListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(getResources().getString(R.string.video_chat));
        setContentView(R.layout.video_chat_layout);
        int placeId = getIntent().getIntExtra("placeId", -1);
        p = (placeId != -1) ? KisiAPI.getInstance().getPlaceById(placeId) : null;
        initViews();
    }

    private void initViews() {
        Debugger.logConnection("initViews");

        // Setup UI
        //
        opponentView = (OpponentGlSurfaceView) findViewById(R.id.opponentView);

        cameraView = (CameraView) findViewById(R.id.cameraView);
        cameraView.setCameraFrameProcess(true);
        // Set VideoChat listener
        cameraView.setQBVideoChatListener(qbVideoChatListener);

        // Set Camera init callback
        cameraView.setFPS(6);
        cameraView.setVisibility(View.VISIBLE);
        cameraView.setOnCameraViewListener(new OnCameraViewListener() {
            @Override
            public void onCameraSupportedPreviewSizes(List<Camera.Size> supportedPreviewSizes) {
//                cameraView.setFrameSize(supportedPreviewSizes.get(5));
                Camera.Size firstFrameSize = supportedPreviewSizes.get(0);
                Camera.Size lastFrameSize = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
                cameraView.setFrameSize(firstFrameSize.width > lastFrameSize.width ? lastFrameSize : firstFrameSize);
            }
        });

        opponentImageLoadingPb = (ProgressBar) findViewById(R.id.opponentImageLoading);

        // VideoChat settings
        videoChatConfig = getIntent().getParcelableExtra(VideoChatConfig.class.getCanonicalName());

        try {
            QBVideoChatController.getInstance().setQBVideoChatListener(QuickBloxApi.getInstance().getCurrentQbUser(), qbVideoChatListener);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        mLockList = (ListView) findViewById(R.id.video_chat_lock_list);
        if(p == null){
        	mLockList.setVisibility(View.GONE);
        	
        }else{
        	mLockList.setVisibility(View.VISIBLE);
    		mLockListAdapter = new LockListAdapter(this, p.getId());
    		mLockList.setAdapter(mLockListAdapter);
    		mLockList.setOnItemClickListener(new LockListOnItemClickListener(p));
    		mLockList.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.reuseCameraView();
    }

    @Override
    protected void onPause() {
        cameraView.closeCamera();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);
        super.onDestroy();
    }

    OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {
        @Override
        public void onCameraDataReceive(byte[] videoData) {
            if (videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
                return;
            }
            QBVideoChatController.getInstance().sendVideo(videoData);
        }

        @Override
        public void onMicrophoneDataReceive(byte[] audioData) {
            QBVideoChatController.getInstance().sendAudio(audioData);
        }

        @Override
        public void onOpponentVideoDataReceive(byte[] videoData) {
            opponentView.loadOpponentImage(videoData);
        }

        @Override
        public void onOpponentAudioDataReceive(byte[] audioData) {
            QBVideoChatController.getInstance().playAudio(audioData);
        }

        @Override
        public void onProgress(boolean progress) {
            opponentImageLoadingPb.setVisibility(progress ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onVideoChatStateChange(CallState callState, VideoChatConfig chat) {
            switch (callState) {
                case ON_CALL_START:
                    Toast.makeText(getBaseContext(), getString(R.string.call_start), Toast.LENGTH_SHORT).show();
                    break;
                case ON_CANCELED_CALL:
                    Toast.makeText(getBaseContext(), getString(R.string.call_canceled_by_caller), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ON_CALL_END:
                    finish();
                    break;
            }
        }
    };
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.video_chat, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.endVideoChat:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
