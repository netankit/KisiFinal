package de.kisi.android;

import java.util.Hashtable;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceFragment extends Fragment {

	final Fragment fragment = this;
	private ScrollView layout;
	private final static long delay = 1000;
	private int index;
	private Lock lockToUnlock;
	private Activity mActivity;
	
	
	private Hashtable<Integer, Button> buttonHashtable;
	
	static PlaceFragment newInstance(int index) {
		// Fragments must not have a custom constructor
		PlaceFragment f = new PlaceFragment();
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);		
		return f;
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		
		buttonHashtable = new Hashtable<Integer, Button>();

		layout = (ScrollView) inflater.inflate(R.layout.place_fragment, container, false);
		
		index = getArguments().getInt("index");
		
		final Place place = KisiAPI.getInstance().getPlaceAt(index);
		if(place != null)
			setupButtons(place);
		/*if(lockToUnlock == null)  {
		KisiAPI.getInstance().updateLocks(place, new OnPlaceChangedListener() {

				@Override
				public void onPlaceChanged(Place[] newPlaces) {
					setupButtons(place);
				}
			
			});
		}*/
		unlockLock();
		
		return layout;
	}

	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}
	
	private void setupButtons(final Place place) {
		
		//check if fragment is attached
		 if(mActivity == null) {
		 	return;
		 }
		
		Drawable lockIcon = mActivity.getResources().getDrawable(R.drawable.kisi_lock);
		
		Typeface font = Typeface.createFromAsset(mActivity.getApplicationContext().getAssets(), "Roboto-Light.ttf");
		//Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());
		
		LinearLayout ly = (LinearLayout) layout.getChildAt(0);
		ly.removeAllViews();

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		layoutParams.setMargins(margin, margin, margin, margin);

		//show a text if there is no lock for a place
		if(place.getLocks().size() == 0) {
			final TextView text = new TextView(mActivity);
			text.setText(R.string.no_lock);
			text.setTypeface(font);
			text.setGravity(Gravity.CENTER);
			text.setWidth(width);
			text.setHeight(height);
			text.setTextColor(Color.WHITE);
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			text.setVisibility(View.VISIBLE);
			ly.addView(text, layoutParams);
			return;
		}    
		
		for (final Lock lock : place.getLocks()) {
			
			final Button button;
			
			if(buttonHashtable.containsKey(lock.getId())) {
				button = buttonHashtable.get(lock.getId());
				
			} else {
				button = new Button(mActivity);
				button.setText(lock.getName());
				button.setTypeface(font);
				button.setGravity(Gravity.CENTER);
				button.setWidth(width);
				button.setHeight(height);
				button.setTextColor(Color.WHITE);
				
				button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
				button.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, null, null);
				button.setVisibility(View.VISIBLE);

				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						final ProgressDialog progressDialog = new ProgressDialog(mActivity);
						progressDialog.setMessage(fragment.getString(R.string.opening));
						progressDialog.show();

						KisiAPI.getInstance().unlock(lock, new UnlockCallback(){
							
							
							@Override
							public void onUnlockSuccess(String message) {
								progressDialog.dismiss();
								changeButtonStyleToUnlocked(button, lock, message);
							}

							@Override
							public void onUnlockFail(String alertMsg) {
								progressDialog.dismiss();
								changeButtonStyleToFailure(button, lock, alertMsg);
							}});
					}
				});
				
				buttonHashtable.put(lock.getId(), button);
			}
			
			ly.addView(button, layoutParams);
		}

		
	}
	
	private void unlockLock() {
		if(lockToUnlock != null) {
			if(buttonHashtable.containsKey(lockToUnlock.getId())) {
				Button button = buttonHashtable.get(lockToUnlock.getId());
				layout.scrollTo(0, button.getTop());
				button.callOnClick();
			}
			lockToUnlock = null;
		}
	}
	

	@SuppressWarnings("deprecation")
	public void changeButtonStyleToUnlocked(Button button, Lock lock, String message) {
		layout.requestLayout();
		Toast.makeText(this.mActivity, message, Toast.LENGTH_SHORT).show();
		final Button currentButton = buttonHashtable.get(lock.getId());
		// save button design
		final Drawable currentBackground = currentButton.getBackground();
		//final Button currentButton = button;
		final String currentText = (String) button.getText();

		// change to unlocked design
		currentButton.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.unlocked));
		currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock_open2, 0, 0, 0);
		// disable click
		currentButton.setClickable(false);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				// after delay back to old design re-enable click
				currentButton.setBackgroundDrawable(currentBackground);
				currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
				currentButton.setText(currentText);
				currentButton.setClickable(true);
			}
		}, delay);

	}
	
	
	@SuppressWarnings("deprecation")
	public void changeButtonStyleToFailure(Button button, Lock lock, String message) {
		layout.requestLayout();
		Toast.makeText(this.mActivity, message, Toast.LENGTH_SHORT).show();
		final Button currentButton = buttonHashtable.get(lock.getId());
		// save button design
		final Drawable currentBackground = currentButton.getBackground();
		//final Button currentButton = button;
		final String currentText = (String) button.getText();
		
		// change to failure design
		currentButton.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.lockfailure));
		currentButton.requestLayout();
		// disable click
		currentButton.setClickable(false);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				// after delay back to old design re-enable click
				currentButton.setBackgroundDrawable(currentBackground);
				currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
				currentButton.setText(currentText);
				currentButton.setClickable(true);

			}
		}, delay);

	}

	public void setLockToUnlock(Lock lockToUnlock) {
		this.lockToUnlock = lockToUnlock;
		if(this.isVisible()) {
			unlockLock();
		}
	}
	
	public void setLockToHighlight(Lock lockToUnlock) {
		if(this.isVisible()){
			if(buttonHashtable.containsKey(lockToUnlock.getId())) {
				Button button = buttonHashtable.get(lockToUnlock.getId());
				layout.scrollTo(0, button.getTop());
				Drawable d = button.getBackground();
				int w = button.getWidth();
				int h = button.getHeight();
				Paint p = new Paint();
				p.setARGB(255, 255, 255, 0);
				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
				p.setStrokeWidth(px+1);
				Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(bitmap);
				d.draw(c);
				c.drawLine(0, 0, w, 0, p);
				c.drawLine(0, 0, 0, h, p);
				c.drawLine(0, h, w, h, p);
				c.drawLine(w, 0, w, h, p);
				BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
				button.setBackgroundDrawable(drawable);
			}
		}
	}

}