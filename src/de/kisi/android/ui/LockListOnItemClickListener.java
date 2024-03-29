package de.kisi.android.ui;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class LockListOnItemClickListener implements OnItemClickListener {

	private Place place;
	private static long delay = 1500;
	
	
	public LockListOnItemClickListener(Place place) {
		this.place = place;
	}

	
	@Override
	public void  onItemClick(final AdapterView<?> parent,final View view, final int position, long id) {
		final ProgressDialog progressDialog = new ProgressDialog(parent.getContext());
		String during_action_message = place.getLocks().get(position).getDuringActionMessage();
		if (during_action_message != null && during_action_message.length()>0){
			progressDialog.setMessage(during_action_message);
		}else{
			progressDialog.setMessage(KisiApplication.getInstance().getString(R.string.opening));
		}
		progressDialog.setCancelable(false);
		progressDialog.show();
		final Lock lock = place.getLocks().get(position);
		Log.d("LockListOnItemClick", String.valueOf(parent.getAdapter().getCount()));
		Log.d("LockListOnItemClick", "Position: "+position);
		Log.d("LockListOnItemClick", ((Button)((LinearLayout) view).getChildAt(0)).getText().toString());
		
		LockListAdapter adapter = (LockListAdapter)parent.getAdapter();
		String trigger = adapter.getTrigger(); 
		//TODO: implement this!!!
		String buttonTrigger = "manual";
		boolean automatic = false;
		if("NFC".equals(trigger)){
			buttonTrigger = "NFC";
			automatic = true;
		}
		if("BLE".equals(trigger))
			buttonTrigger = "beacon";
		if("geofence".equals(trigger))
			buttonTrigger = "geofence";
		if(adapter.isSuggestedNFC(lock.getId())){
			buttonTrigger = "NFC";
			automatic = false;
		}
		
		
		
		
		KisiAPI.getInstance().unlock(lock, new UnlockCallback(){
			
			
			@Override
			public void onUnlockSuccess(String message) {
				progressDialog.dismiss();
				String after_action_message = lock.getAfterActionMessage();
				if(after_action_message != null && after_action_message.length()>0){
					Toast.makeText(KisiApplication.getInstance(), after_action_message, Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(KisiApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
				}
				final Button currentButton = (Button) ((LinearLayout) parent.getChildAt(position)).getChildAt(0);
				parent.invalidate();
				// save button design
				//final Drawable currentBackground = currentButton.getBackground();
				final String currentText = (String) currentButton.getText();

				// change to unlocked design
				currentButton.setText("");
				currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock_open2, 0, 0, 0);
				currentButton.setBackgroundColor(Color.GREEN);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
						//currentButton.setBackgroundDrawable(currentBackground);
						currentButton.setBackgroundColor(KisiApplication.getInstance().getResources().getColor(R.color.kisi_color));
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
						currentButton.setText(currentText);

					}
				}, delay);
			}

			@Override
			public void onUnlockFail(String alertMsg) {
				progressDialog.dismiss();
				Toast.makeText(KisiApplication.getInstance(), alertMsg, Toast.LENGTH_SHORT).show();
//				Log.d("LockListOnItemClickListener" , view.toString() + "  " + ((Button) view).getText());
//				Log.d("LockListOnItemClickListener" , parent.getChildAt(position).toString() + "  " + ((Button)parent.getChildAt(position)).getText());
				Button button = null ;
				if( parent.getChildAt(position) == null) {
					button = (Button) view;
				}
				else {
					button = (Button) ((LinearLayout) parent.getChildAt(position)).getChildAt(0);
				}
				final Button currentButton = button;
				parent.invalidate();

//						(Button) parent.getChildAt(position);
				// save button design
				//final Drawable currentBackground = currentButton.getBackground();
				final String currentText = (String) currentButton.getText();
				// change to failure design
				currentButton.setBackgroundColor(Color.RED);
				currentButton.setText(alertMsg);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
//						currentButton.setBackgroundDrawable(currentBackground);
						currentButton.setBackgroundColor(KisiApplication.getInstance().getResources().getColor(R.color.kisi_color));
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
						currentButton.setText(currentText);
					}
				}, delay);

			}				
			},buttonTrigger, automatic);

	
	}
	

}
