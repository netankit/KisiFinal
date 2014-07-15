package de.kisi.android.messages;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.kisi.android.Config;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;
import de.kisi.android.ui.CallDialogActivity;

public class GCMIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;

	private static final String TAG = GCMIntentService.class.getSimpleName();

	private NotificationManager notificationManager;

	public GCMIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = googleCloudMessaging.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM
			 * will be extended in the future with new message types, just ignore
			 * any message types you're not interested in, or that you don't
			 * recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Log.i(TAG, "Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				Log.i(TAG, "Deleted message: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				Log.i(TAG, "Received message: " + extras.toString());
				// If it's a regular GCM message, do some verifications and show ring dialog

				String messageJson = extras.getString("message");
				if(messageJson != null){
					Log.d("GCMImtentService", "Received json: " + messageJson);
					try{
						Message message = new Gson().fromJson(messageJson, Message.class);

						//verify, that the message is for the current user and that it is a ring
						if(KisiAPI.getInstance().getUser() == null) return;
						if(message.getReceiver()==KisiAPI.getInstance().getUser().getId() && message.getType().equals("ring")){
							//verify, that the message was sent recently (not older than 1 minute)
							if(message.getTime()>System.currentTimeMillis()-60000){
								//TODO optional: if the user don't want to get called, ignore the message
								//TODO optional: verify, that the sender is not blocked by the user
								Log.d("GCMImtentService", "Received ring from: " + message.getSender());
								// Post notification

								Intent callDialog = new Intent(this, CallDialogActivity.class);
								callDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								Bundle bundle = new Bundle();  
								bundle.putSerializable("message", message);
								callDialog.putExtras(bundle);
								startActivity(callDialog);
							}
						}
					}catch (JsonSyntaxException e){
						Log.w("GCMImtentService", "Received invalid message.");
					}
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	private void sendNotification(String msg) {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
				R.drawable.notification_icon).setContentTitle("GCM Notification").setStyle(
						new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);
		notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		//TODO: audio notification
		//TODO: if notification is clicked, show the activity, where the user can decide if he wants to start a call
	}
}