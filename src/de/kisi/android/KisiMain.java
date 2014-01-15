package de.kisi.android;

import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

public class KisiMain extends FragmentActivity implements
		PopupMenu.OnMenuItemClickListener {

	private static final String API_KEY = "08a6dd6db0cd365513df881568c47a1c";

	private ViewPager pager;
	private KisiAPI kisiAPI;
	private PlaceFragmentPagerAdapter pagerAdapter;
	private int pageNumber = 0;

	// just choose a random value
	// TODO: change this later
	public static int LOGIN_REQUEST_CODE = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kisiAPI = KisiAPI.getInstance();

		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.kisi_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.window_title);

		pager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public void onStart() {
		super.onStart();

		kisiAPI.updatePlaces(new OnPlaceChangedListener() {

			@Override
			public void onPlaceChanged(Place[] newPlaces) {
				setupView(newPlaces);

			}

		});

	}

	// creating popup-menu for settings
	public void showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.settings, popup.getMenu());
		popup.setOnMenuItemClickListener(this);

		popup.show();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d("OnNewIntent", intent.getExtras().toString());
		if (intent.getExtras() != null)
			if (intent.getStringExtra("Type").equals("unlock")) {

				for (int j = 0; j < kisiAPI.getPlaces().length; j++) {
					if (kisiAPI.getPlaces()[j].getId() == intent.getIntExtra(
							"Place", -1)) {
						pager.setCurrentItem(j, false);
						PlaceFragment placeFragment = (PlaceFragment) pagerAdapter.getItem(j);
						placeFragment.unlockLock(intent.getIntExtra("Lock", -1));
						pageNumber = j;
					}
				}

			}

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// get all places
		Place[] places = kisiAPI.getPlaces();

		switch (item.getItemId()) {
		case R.id.refresh:
			kisiAPI.refresh(new OnPlaceChangedListener() {

				@Override
				public void onPlaceChanged(Place[] newPlaces) {
					setupView(newPlaces);
				}

			});
			return true;

		case R.id.share:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.share_empty_place_error,
						Toast.LENGTH_LONG).show();
				return false;
			}

			Place p = places[pager.getCurrentItem()];
			// check if user is owner
			if (!kisiAPI.userIsOwner(p)) {
				Toast.makeText(this, R.string.share_owner_only,
						Toast.LENGTH_LONG).show();
				return false;
			}

			Intent intent = new Intent(getApplicationContext(),
					ShareKeyActivity.class);
			intent.putExtra("place", pager.getCurrentItem());
			startActivity(intent);
			return true;

		case R.id.showLog:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.log_empty_place_error,
						Toast.LENGTH_LONG).show();
				return false;
			}
			Place place = places[pager.getCurrentItem()];
			Intent logView = new Intent(getApplicationContext(), LogInfo.class);
			logView.putExtra("place_id", place.getId());
			startActivity(logView);

			return true;

		case R.id.setup:

			BlinkupController blinkup = BlinkupController.getInstance();
			blinkup.intentBlinkupComplete = new Intent(this,
					BlinkupCompleteActivity.class);

			if (kisiAPI.getUser().getEiPlanId() != null)
				blinkup.setPlanID(kisiAPI.getUser().getEiPlanId());

			blinkup.selectWifiAndSetupDevice(this, API_KEY,
					new ServerErrorHandler() {
						@Override
						public void onError(String errorMsg) {
							Toast.makeText(getApplicationContext(), errorMsg,
									Toast.LENGTH_SHORT).show();
						}
					});
			return true;

		case R.id.logout:
			logout();
			return true;

		default:
			return false;
		}

	}

	// callback for blinkup
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE) {
			if (resultCode == AccountPickerActivity.LOGIN_FAILED) {
				finish();
				return;
			}
			if (resultCode == AccountPickerActivity.LOGIN_SUCCESS) {
				setupView(kisiAPI.getPlaces());
				return;
			}
		} else {
			BlinkupController.getInstance().handleActivityResult(this,
					requestCode, resultCode, data);
		}
	}

	private void logout() {
		kisiAPI.logout();
		finish();
	}

	// public void onBackPressed() {
	// moveTaskToBack(true);
	// }

	private void setupView(Place[] places) {

		List<PlaceFragment> fragments = new Vector<PlaceFragment>();
		
		for (int j = 0; j < places.length; j++)
			fragments.add(PlaceFragment.newInstance(j));

		FragmentManager fm = getSupportFragmentManager();
		pagerAdapter = new PlaceFragmentPagerAdapter(
				fm, fragments);
		pager.setAdapter(pagerAdapter);
		
		//prevents that when app got start by clicking on a notification the fragment corresponding to the notification is shown
		if(pageNumber < pagerAdapter.getCount()) {
			pager.setCurrentItem(pageNumber, false);
			}
			
	}

}
