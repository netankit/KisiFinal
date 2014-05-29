package de.kisi.android;

import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LockHandler;
import de.kisi.android.api.LoginHandler;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.api.PlacesHandler;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class KisiMain extends BaseActivity implements PopupMenu.OnMenuItemClickListener {

	private static final String API_KEY = "08a6dd6db0cd365513df881568c47a1c";

	private ViewPager pager;
	private KisiAPI kisiAPI;
	private PlacesHandler placesHandler;
	private LoginHandler loginHandler;
	private PlaceFragmentPagerAdapter pagerAdapter;

	private int currentPage = 0;

	// just choose a random value
	// TODO: change this later
	public static int LOGIN_REQUEST_CODE = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		NewRelic.withApplicationToken("AAe80044cf73854b68f6e83881c9e61c0df9d92e56").start(this.getApplication());
		
		kisiAPI = KisiAPI.getInstance();
		placesHandler = PlacesHandler.getInstance();
		loginHandler = LoginHandler.getInstance();

		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.kisi_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.window_title);

		FragmentManager fm = getSupportFragmentManager();
		pagerAdapter = new PlaceFragmentPagerAdapter(fm);
		pager = (ViewPager) findViewById(R.id.pager);
	
	}

	@Override
	protected void onStart() {
		super.onStart();
		buildUI();
		
		//receives intents when activity is started by an intent
		if(getIntent().hasExtra("Type")) {
			handleUnlockIntent(getIntent());
			//removing the unlock intent, because otherwise the lock would be unlock every time the app is start from the background
			getIntent().removeExtra("Type");
		}
	}
	
	//receives intents when activity is already running
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if(intent.hasExtra("Type")) {
			handleUnlockIntent(intent);
			intent.removeCategory("Type");
		}
	}

	
	private void buildUI() { 
		Place[] places;
		if((places = placesHandler.getPlaces()) != null) {
			// build the UI now with persistent data
			setupView(places);
		}
		
		placesHandler.updatePlaces(new OnPlaceChangedListener() {
			@Override
			public void onPlaceChanged(Place[] newPlaces) {
				// build the UI again with fresh data from the server
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
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// get all places
		Place[] places = placesHandler.getPlaces();

		switch (item.getItemId()) {
		case R.id.refresh:
			// Trigger a refresh of the data
			placesHandler.refresh(new OnPlaceChangedListener() {

				@Override
				public void onPlaceChanged(Place[] newPlaces) {
					// refresh the view with the new data
					setupView(newPlaces);
				}

			});
			return true;

		case R.id.share:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.share_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}

			Place p = places[pager.getCurrentItem()];
			// check if user is owner
			if (!placesHandler.userIsOwner(p)) {
				Toast.makeText(this, R.string.share_owner_only, Toast.LENGTH_LONG).show();
				return false;
			}

			Intent intent = new Intent(getApplicationContext(), ShareKeyActivity.class);
			intent.putExtra("place", pager.getCurrentItem());
			startActivity(intent);
			return true;

		case R.id.showLog:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.log_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}
			Place place = places[pager.getCurrentItem()];
			Intent logView = new Intent(getApplicationContext(), LogInfo.class);
			logView.putExtra("place_id", place.getId());
			startActivity(logView);

			return true;

		case R.id.setup:

			BlinkupController blinkup = BlinkupController.getInstance();
			blinkup.intentBlinkupComplete = new Intent(this, BlinkupCompleteActivity.class);

			if (kisiAPI.getUser().getEiPlanId() != null)
				blinkup.setPlanID(kisiAPI.getUser().getEiPlanId());

			blinkup.selectWifiAndSetupDevice(this, API_KEY,
					new ServerErrorHandler() {
						@Override
						public void onError(String errorMsg) {
							Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
						}
					});
			return true;
			
		case R.id.notification:
			Intent settingsIntent = new Intent(this, PlaceNotificationSettings.class);
			startActivity(settingsIntent);
			return true;
			
		case R.id.logout:
			logout();
			return true;
			
		case R.id.about_version:
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.kisi);
			String versionName = null;
			try {
				versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			alertDialog.setMessage(getResources().getString(R.string.version) + versionName);
			alertDialog.show();
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
				buildUI();
				return;
			}
		} else {
			BlinkupController.getInstance().handleActivityResult(this,requestCode, resultCode, data);
		}
	}

	private void logout() {
		loginHandler.logout();
		finish();
	}

	private void setupView(Place[] places) {
		currentPage = pager.getCurrentItem();
		
		List<PlaceFragment> fragments = new Vector<PlaceFragment>();
		
		for (int j = 0; j < places.length; j++)
			fragments.add(PlaceFragment.newInstance(j));
	
		pagerAdapter.setFragementList(fragments);
		if(pager.getAdapter() == null) {
			pager.setAdapter(pagerAdapter);
		}
		
		//when view get refreshed to pager should show that fragment, which was shown before 
		//if place got delete the pager shows the first fragment
		if(currentPage < pagerAdapter.getCount()) {
			pager.setCurrentItem(currentPage, false);
		}
		else {
			pager.setCurrentItem(0, false);
		}
		pager.invalidate();
	}

	private void handleUnlockIntent(Intent intent) {
		// No extras, nothing to do
		if (intent.getExtras() == null)
			return;
		
		// Its not a unlock request, nothing to do
		if (!intent.getStringExtra("Type").equals("unlock"))
			return;
		
		
		int placeId = intent.getIntExtra("Place", -1);
		for (int j = 0; j < placesHandler.getPlaces().length; j++) {
			if (placesHandler.getPlaces()[j].getId() == placeId) {
				int lockId = intent.getIntExtra("Lock", -1);
				Lock lockToUnlock = LockHandler.getInstance().getLockById(placesHandler.getPlaceById(placeId), lockId);
				pager.setCurrentItem(j, false);
				int id  = pager.getCurrentItem();
				// http://tofu0913.blogspot.de/2013/06/adnroid-get-current-fragment-when-using.html
				// BAD HACK see: String android.support.v4.app.FragmentPagerAdapter.makeFragmentName(int viewId, long id)
				PlaceFragment placeFragment = (PlaceFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+id); 
				currentPage = j;
				//check if fragment got already attach to the pager and otherwise get fragment from pagerAdapter
				if(placeFragment != null) {
					placeFragment.setLockToUnlock(lockToUnlock);
				}
				else {
					placeFragment = (PlaceFragment) pagerAdapter.getItem(j);
					placeFragment.setLockToUnlock(lockToUnlock);
				}
				break;
			}
		}
	}
	
		
}
