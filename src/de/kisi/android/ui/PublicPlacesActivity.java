package de.kisi.android.ui;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.module.videochat.model.utils.Debugger;

import de.kisi.android.messages.Message;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.PublicPlacesCallback;
import de.kisi.android.api.QuickBloxApi;
import de.kisi.android.model.Place;

/**
 * This activity shows the public places around the user.
 * 
 */
public class PublicPlacesActivity extends Activity {

	private ListView placeListView;
	public static final int VIDEO_CHAT_ACTIVITY = 1;
	private ProgressDialog progressDialog;
	private VideoChatConfig videoChatConfig;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(getResources().getString(R.string.places_around));
		setContentView(R.layout.public_places);

		placeListView = (ListView) findViewById(R.id.public_places_listview);


		//TODO: only login if you are not already logged in
		QuickBloxApi.getInstance().login(new QBCallback() {
			@Override public void onComplete(Result result, Object o) {}
			@Override public void onComplete(Result result) {
				Log.d("PublicPlacesActivity", "Finished login successful: "+result.isSuccess());
				if(result.isSuccess()){
					QuickBloxApi.getInstance().initVideoChat(qbVideoChatListener);
					KisiAPI.getInstance().getPublicPlaces(new PublicPlacesCallback() {
						@Override
						public void onResult(Place[] places) {
							placeListView.setAdapter(new PublicPlacesAdapter(places));
						}
					});
				}else{
					Log.d("PublicPlacesActivity", "Unable to login");
				}
				
			}
		});

		

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.calling_the_place_owner));
        progressDialog.setCancelable(false);

	}

	/**
	 * Wrapper for public places.
	 *
	 */
	class PublicPlacesAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		Place[] places;

		public PublicPlacesAdapter(Place[] places) {
			super();
			this.places=places;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return places.length;
		}

		@Override
		public Place getItem(int position) {
			return places[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (vi == null)
				vi = inflater.inflate(R.layout.public_place_item, null);
			final Place p = this.getItem(position);
			TextView placeName = (TextView) vi.findViewById(R.id.public_place_name);
			placeName.setText(p.getName());
			TextView placeDescription = (TextView) vi.findViewById(R.id.public_place_description);
			String description = p.getAdditionalInformation(); //TODO: clarify what should be displayed here
			if(description != null && ! description.isEmpty()){
				placeDescription.setText(description);
			}else{
				placeDescription.setVisibility(View.GONE);
			}
			Button buttonRing = (Button) vi.findViewById(R.id.public_place_button_ring);
			buttonRing.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					final int myId = (KisiAPI.getInstance().getUser() != null) ? KisiAPI.getInstance().getUser().getId() : -1;
					final int receiverId = p.getOwnerId();
					if(myId != receiverId){
						progressDialog.show();
						deleteDialog();
						final Message message = new Message(QuickBloxApi.getInstance().getCurrentQbUser().getId(), p.getOwnerId(), "ring", p.getId());
	
						QuickBloxApi.getInstance().getUserByLogin("kisi-"+p.getOwnerId(), new QBCallback() {
							@Override
							public void onComplete(Result arg0, Object arg1) {}
							@Override
							public void onComplete(Result result) {
								// TODO check if the receiver is registered for push notifications.
								int receiverID = ((QBUserResult) result).getUser().getId();
								QuickBloxApi.getInstance().sendMessage(message, receiverID);
							}
						});
					}else{
						Toast.makeText(getApplicationContext(), getString(R.string.call_not_possible), Toast.LENGTH_SHORT).show();
					}
				}
			});
			return vi;
		}
	}

	/**
	 *  Listener for the backbutton of the action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Listener for video chat.
	 */
	OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {
		@Override
		public void onVideoChatStateChange(CallState state, VideoChatConfig receivedVideoChatConfig) {
			Debugger.logConnection("onVideoChatStateChange: " + state);
			videoChatConfig = receivedVideoChatConfig;
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			Log.d("PublicPlacesActivity", "VideoChatListener state: " + state.toString());
			switch (state) {
			
			case ACCEPT:
				// TODO Add security checks and timeout.
//				if(progressDialog.isShowing()){
					QuickBloxApi.getInstance().acceptVideoChat(receivedVideoChatConfig);
					startVideoChatActivity();
//				}else{
//					QuickBloxApi.getInstance().rejectVideoChat(receivedVideoChatConfig);
//				}
				break;
			default:
				break;

			}
		}
	};
	
	/**
	 * Creates an intent for the video chat activity and starts that activity with current video chat configuration.
	 */
	private void startVideoChatActivity() {
		Intent intent = new Intent(getBaseContext(), VideoChatActivity.class);
		intent.putExtra(VideoChatConfig.class.getCanonicalName(), videoChatConfig);
		startActivityForResult(intent, VIDEO_CHAT_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VIDEO_CHAT_ACTIVITY) {
			try {
		        progressDialog.dismiss();
				QBVideoChatController.getInstance().setQBVideoChatListener(QuickBloxApi.getInstance().getCurrentQbUser(), qbVideoChatListener);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
//		QuickBloxApi.getInstance().stopVideoChat();
		super.onDestroy();
	}
	
    @Override
    public void onResume() {
        progressDialog.dismiss();
        super.onResume();
    }
    /**
     * It sets a timer and deletes the progress dialog.
     */
    public void deleteDialog(){
		final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
            	progressDialog.dismiss();
            }
        }, 60000);
    }
}
