package de.kisi.android.ui;


import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.definition.VideoChatConstants;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.module.videochat.model.utils.Debugger;

import de.kisi.android.R;
import de.kisi.android.api.QuickBloxApi;
import de.kisi.android.messages.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

/**
 * This activity is started by a push notification and is responsible for showing a call dialog to the user.
 *
 */
public class CallDialogActivity extends Activity{

	private Message message;
	private VideoChatConfig videoChatConfig;
	private CallDialogActivity instance;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		showCallDialog();

		Bundle bundle = getIntent().getExtras();
		if(bundle != null)
			message = (Message) bundle.getSerializable("message");

		if(message == null){
			finish();
		}
		
		QuickBloxApi.getInstance().initVideoChat(qbVideoChatListener);
	}

	private void startVideoChatActivity(int placeId) {
		Intent intent = new Intent(getBaseContext(), VideoChatActivity.class);
		intent.putExtra(VideoChatConfig.class.getCanonicalName(), videoChatConfig);
		intent.putExtra("placeId", placeId);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
//		QuickBloxApi.getInstance().stopVideoChat();
		super.onDestroy();

	};


	OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {
		@Override
		public void onVideoChatStateChange(CallState state, VideoChatConfig receivedVideoChatConfig) {
			Debugger.logConnection("onVideoChatStateChange: " + state);
			videoChatConfig = receivedVideoChatConfig;

			switch (state) {
			case ON_ACCEPT_BY_USER:
				QBVideoChatController.getInstance().onAcceptFriendCall(videoChatConfig, null);
				startVideoChatActivity(message.getPlace());
				break;
			case ON_REJECTED_BY_USER:
				Toast.makeText(getApplicationContext(), getString(R.string.user_not_available), Toast.LENGTH_SHORT).show();
				break;
			case ON_DID_NOT_ANSWERED:
				Toast.makeText(getApplicationContext(), getString(R.string.communication_error), Toast.LENGTH_SHORT).show();
				break;
			case ON_CANCELED_CALL:
				videoChatConfig = null;
				break;
			}
		}
	};

	/**
	 * Shows the call dialog.
	 */
	private void showCallDialog() {
		autoCancelHandler.postDelayed(autoCancelTask, 10000);
		alertDialog = showCallDialog(this, new OnCallDialogListener() {
			@Override
			public void onAcceptCallClick() {
//				QuickBloxApi.getInstance().initVideoChat(qbVideoChatListener);
				QBVideoChatController.getInstance().callFriend(new QBUser(message.getSender()), CallType.VIDEO_AUDIO, null);
			}

			@Override
			public void onRejectCallClick() {
				finish();
				return;
			}
		});
	}
	private AlertDialog alertDialog;
	private Handler autoCancelHandler = new Handler(Looper.getMainLooper());
	private Runnable autoCancelTask = new Runnable() {
		@Override
		public void run() {
			if (alertDialog != null && alertDialog.isShowing()){
				instance.finish();
			}
		}
	};

	private static AlertDialog.Builder builder;
	public static AlertDialog showCallDialog(Context context, final OnCallDialogListener callDialogListener) {
		if (builder == null) {
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						callDialogListener.onAcceptCallClick();
						deleteCallDialog(2000);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						callDialogListener.onRejectCallClick();
						deleteCallDialog(2000);
						break;
					}
				}
			};
			builder = new AlertDialog.Builder(context);
			//TODO Add String resource
			builder.setTitle("Kisi Video Chat")
			.setMessage("Calling...")
			.setPositiveButton(VideoChatConstants.YES, onClickListener)
			.setNegativeButton(VideoChatConstants.NO, onClickListener)
			.show();
		}
		

		return builder.create();
	}


	private static void deleteCallDialog(int delay) {
		final Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				builder = null;
			}
		}, delay);
	}

	/**
	 * Interface for call dialog.
	 *
	 */
	public interface OnCallDialogListener {
		public void onAcceptCallClick();
		public void onRejectCallClick();
	}

}
