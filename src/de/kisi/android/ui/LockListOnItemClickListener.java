package de.kisi.android.ui;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LockListOnItemClickListener implements OnItemClickListener {

	private Place place;
	private static long delay = 1500;
	
	
	public LockListOnItemClickListener(Place place) {
		this.place = place;
	}
	
	

	
	@Override
	public void  onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
		final ProgressDialog progressDialog = new ProgressDialog(view.getContext());
		progressDialog.setMessage(KisiApplication.getInstance().getString(R.string.opening));
		progressDialog.setCancelable(false);
		progressDialog.show();
		final Lock lock = place.getLocks().get(position);
		KisiAPI.getInstance().unlock(lock, new UnlockCallback(){
			
			
			@Override
			public void onUnlockSuccess(String message) {
				progressDialog.dismiss();
				Toast.makeText(KisiApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
				final Button currentButton = (Button) parent.getChildAt(position);
				// save button design
				final Drawable currentBackground = currentButton.getBackground();
				final String currentText = (String) currentButton.getText();

				// change to unlocked design
				currentButton.setText("");
				currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock_open2, 0, 0, 0);
				currentButton.setBackgroundColor(Color.GREEN);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
						currentButton.setBackgroundDrawable(currentBackground);
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
						currentButton.setText(currentText);

					}
				}, delay);	
			}

			@Override
			public void onUnlockFail(String alertMsg) {
				progressDialog.dismiss();
				Toast.makeText(KisiApplication.getInstance(), alertMsg, Toast.LENGTH_SHORT).show();
				final Button currentButton = (Button) parent.getChildAt(position);
				// save button design
				final Drawable currentBackground = currentButton.getBackground();
				final String currentText = (String) currentButton.getText();

				// change to failure design
				currentButton.setBackgroundColor(Color.RED);
				currentButton.setText("");

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
						currentButton.setBackgroundDrawable(currentBackground);
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
						currentButton.setText(currentText);
					}
				}, delay);

			}				
			});

	
	}
	

}
