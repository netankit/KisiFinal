package de.kisi.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.manavo.rest.RestCache;
import com.manavo.rest.RestCallback;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

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

		int index = getArguments().getInt("index");
		final Place place = ((KisiMain) getActivity()).getPlaces().valueAt(index);

		layout = (RelativeLayout) inflater.inflate(R.layout.place_fragment,
				container, false);

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
		int[] buttons = { R.id.buttonLockOne, R.id.buttonLockTwo,
				R.id.buttonLockThree };

		Typeface font = Typeface.createFromAsset(getActivity()
				.getApplicationContext().getAssets(), "Roboto-Light.ttf");

		int i = 0;
		for (final Lock lock : place.getLocks()) {
			if (i >= buttons.length) {
				Log.d("waring", "more locks then buttons!");
				break;
			}

			final Button button = (Button) layout.findViewById(buttons[i]);
			button.setText(lock.getName());
			button.setTypeface(font);
			button.setVisibility(View.VISIBLE);
			i++;

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					// unlock button was pressed
					// setup api call to open door
					KisiApi api = new KisiApi(getActivity());

					// Add gps coordinates to access request if user has only
					// guest key
					if (place.getOwnerId() != KisiApi.getUserId()) {
						updateLocation();

						try {
							if ( currentLocation != null ) {
								JSONObject location = new JSONObject();
								location.put("latitude", currentLocation.getLatitude());
								location.put("longitude", currentLocation.getLongitude());
								api.addParameter("location", location);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					api.setCallback(new RestCallback() {
						public void success(Object obj) {
							// change button design
							changeButtonStyleToUnlocked(button, lock);
						}

					});
					api.setLoadingMessage(R.string.opening);
					api.post(String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId()));
				}

			});
		}
		// set unused buttons to gone, so the automatic layout works
		for (; i < buttons.length; i++) {
			Button button = (Button) layout.findViewById(buttons[i]);

			button.setVisibility(View.GONE);

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

	public void changeButtonStyleToUnlocked(Button button, Lock lock) {
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

}