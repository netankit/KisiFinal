package de.kisi.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.manavo.rest.RestCache;
import com.manavo.rest.RestCallback;
import com.manavo.rest.RestErrorCallback;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceFragment extends Fragment {

	private RelativeLayout layout;
	private final static long delay = 3000;
	private Location currentLocation;
	private LocationManager locationManager;

	static PlaceFragment newInstance(int index) {
		// Fragments must not have a custom constructor
		PlaceFragment f = new PlaceFragment();

		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		layout = (RelativeLayout) inflater.inflate(R.layout.place_fragment,
				container, false);
		
		
		int index = getArguments().getInt("index");
		KisiMain activity = ((KisiMain) getActivity());
		SparseArray<Place> places = activity.getPlaces();
		// Workaround for crash when starting app from background
		if (places == null) {
			return layout;
		}
		final Place place = places.valueAt(index);

		// get locks from api, if not already available
		if (place.getLocks() == null) {
			KisiApi api = new KisiApi(this.getActivity());

			api.setCallback(new RestCallback() {
				public void success(Object obj) {
					JSONArray data = (JSONArray) obj;

					place.setLocks(data);
					setupButtons(place);
				}

			});
			api.setCachePolicy(RestCache.CachePolicy.CACHE_THEN_NETWORK);
			api.setLoadingMessage(null);
			api.get("places/" + String.valueOf(place.getId()) + "/locks");
		} else {
			setupButtons(place);
		}

		return layout;
	}

	public void setupButtons(final Place place) {
		
		Drawable lockIcon = getActivity().getResources().getDrawable(R.drawable.kisi_lock);
		
		Typeface font = Typeface.createFromAsset(getActivity()
				.getApplicationContext().getAssets(), "Roboto-Light.ttf");
		//Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());
		
		ScrollView sv =  (ScrollView) layout.getChildAt(0);
		LinearLayout ly = (LinearLayout) sv.getChildAt(0);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		
		 //show a text if there is no lock for a place
		if(place.getLocks().size() == 0) {
			final TextView text = new TextView(getActivity());
			text.setText(R.string.no_lock);
			text.setTypeface(font);
			text.setGravity(Gravity.CENTER);
			text.setWidth(width);
			text.setHeight(height);
			text.setTextColor(Color.WHITE);
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			text.setVisibility(View.VISIBLE);
			layoutParams.setMargins(margin, margin*2, margin, margin);
			ly.addView(text, layoutParams);
			return;
		}    
		
		
		int i = 0;
		for (final Lock lock : place.getLocks()) {
			
			final Button button = new Button(getActivity());
			button.setText(lock.getName());
			button.setTypeface(font);
			button.setGravity(Gravity.CENTER);
			button.setWidth(width);
			button.setHeight(height);
			button.setTextColor(Color.WHITE);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			button.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, null, null);
			button.setVisibility(View.VISIBLE);
		
			if(i == 0)
				layoutParams.setMargins(margin, margin*2, margin, margin);
			else
				layoutParams.setMargins(margin, margin, margin, margin);
			ly.addView(button, layoutParams);
			i++;

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					// unlock button was pressed
					// setup api call to open door
					KisiApi api = new KisiApi(getActivity());

					try {
						if (currentLocation != null) {
							JSONObject location = new JSONObject();
							location.put("latitude",
									currentLocation.getLatitude());
							location.put("longitude",
									currentLocation.getLongitude());
							api.addParameter("location", location);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				
					api.setCallback(new RestCallback() {
						public void success(Object obj) {
							JSONObject json = (JSONObject) obj;
							String message = null;
							if(json.has("success")) {
								// change button design
								try {
									message = json.getString("notice");
								} catch (JSONException e) {
									e.printStackTrace();
								}	
								changeButtonStyleToUnlocked(button, lock, message);
								return;
							}
							if(json.has("alert")) {
								try {
									message = json.getString("alert");
								} catch (JSONException e) {
									e.printStackTrace();
								}	
								changeButtonStyleToFailure(button, lock, message);
								return;
							}
						}	
					});
					api.setErrorCallback(new RestErrorCallback () {

						@Override
						public void error(String message) {
							//change RestApi to avoid json parsing here?
							JSONObject json = null;
							try {
								json = new JSONObject(message);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(json.has("alert")) {
								String alertMsg = null;
								try {
									alertMsg = json.getString("alert");
								} catch (JSONException e) {
									e.printStackTrace();
								}
								changeButtonStyleToFailure(button, lock, alertMsg);
								return;
							}
	
						}});
					api.setLoadingMessage(R.string.opening);
					api.post(String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId()));
				}

			});
		}

		
	}
	

	private void updateLocation() {

		LocationListener locListener = new MyLocationListener();
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		// first check Network Connection
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { 
			locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locListener);
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			currentLocation = location;

		}
		// then the GPS Connection
		else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { 
			locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locListener);
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			currentLocation = location;
		}
		// TODO What happens if nothing of both is enabled?

	}

	public void changeButtonStyleToUnlocked(Button button, Lock lock, String message) {
		Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
		// save button design
		final Drawable currentBackground = button.getBackground();
		final Button currentButton = button;
		final String currentText = (String) button.getText();
		final int actualPadding = currentButton.getPaddingLeft();
		final float density = getActivity().getResources().getDisplayMetrics().density;
		final int shift = (int) (138 * density); // 95

		// change to unlocked design

		currentButton.setBackgroundDrawable(getActivity().getResources()
				.getDrawable(R.drawable.unlocked));
		currentButton.setPadding(shift, 0, 0, 0);
		currentButton.setText("");
		// TODO localize?
		currentButton.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.kisi_lock_open2, 0, 0, 0);

		// disable click
		currentButton.setClickable(false);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				// after delay back to old design re-enable click
				currentButton.setBackgroundDrawable(currentBackground);
				currentButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.kisi_lock, 0, 0, 0);
				currentButton.setPadding(actualPadding, 0, 0, 0);
				currentButton.setText(currentText);
				currentButton.setClickable(true);

			}
		}, delay);

	}
	
	
	public void changeButtonStyleToFailure(Button button, Lock lock, String message) {
		Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
		// save button design
		final Drawable currentBackground = button.getBackground();
		final Button currentButton = button;
		final String currentText = (String) button.getText();
		final int actualPadding = currentButton.getPaddingLeft();
		final float density = getActivity().getResources().getDisplayMetrics().density;
		final int shift = (int) (138 * density); // 95

		// change to failure design

		currentButton.setBackgroundDrawable(getActivity().getResources()
				.getDrawable(R.drawable.lockfailure));
		currentButton.setPadding(shift, 0, 0, 0);
		currentButton.setText("");

		// disable click
		currentButton.setClickable(false);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				// after delay back to old design re-enable click
				currentButton.setBackgroundDrawable(currentBackground);
				currentButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.kisi_lock, 0, 0, 0);
				currentButton.setPadding(actualPadding, 0, 0, 0);
				currentButton.setText(currentText);
				currentButton.setClickable(true);

			}
		}, delay);

	}
	
	

}