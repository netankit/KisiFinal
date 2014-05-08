package de.kisi.android.ui;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.BaseActivity;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class KisiMainActivity extends BaseActivity implements OnPlaceChangedListener{
    
	private static final String IMP_API_KEY = "08a6dd6db0cd365513df881568c47a1c";
	private static final String NEW_RELIC_API_KEY = "AAe80044cf73854b68f6e83881c9e61c0df9d92e56";
	
	
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerListAdapter mDrawerListAdapter;
    private ListView mLockList;
    private LockListAdapter mLockListAdapter; 
    private ActionBarDrawerToggle mDrawerToggle;

    
	// just choose a random value
	// TODO: change this later
	public static int LOGIN_REQUEST_CODE = 5;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this.getApplication());
		
		setContentView(R.layout.navigation_drawer_layout);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mLockList = (ListView) findViewById(R.id.lock_list);

		mDrawerListAdapter = new DrawerListAdapter(this);
		mDrawerList.setAdapter(mDrawerListAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		 
		mDrawerToggle = new ActionBarDrawerToggle( this,  mDrawerLayout, R.drawable.logo,  R.string.place_overview,  R.string.kisi ) {
	            public void onDrawerClosed(View view) {
	                getActionBar().setTitle(getResources().getString(R.string.kisi));
//	                super.onDrawerClosed(view);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	                getActionBar().setTitle(getResources().getString(R.string.place_overview));
//	                super.onDrawerOpened(drawerView);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
		 };
		 
		 
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setHomeButtonEnabled(true);
		 
		 
		KisiAPI.getInstance().registerOnPlaceChangedListener(this);
		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(intent.hasExtra("Type")) {
			handleUnlockIntent(intent);
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	    	selectItem(position, id);
	    }
	}
	
	private void setUiIntoStartState() {
		Place place = KisiAPI.getInstance().getPlaceAt(0);
		if(place != null) {
			selectItem(0, place.getId());
		}
		else {
			//check if user is even login 
			if(KisiAPI.getInstance().getUser() != null) {
				//TODO: implement so dialog here
			}
				
			
		}
	}
	
	
	private void selectItem(int position, long id) {
		mLockListAdapter = new LockListAdapter(this, (int) id);
		mLockList.setAdapter(mLockListAdapter);
		mLockList.setOnItemClickListener(new LockListOnItemClickListener(KisiAPI.getInstance().getPlaceById((int) id)));
		mLockList.invalidate();
	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...
		// get all places
		Place[] places = KisiAPI.getInstance().getPlaces();
		Place place;
		switch (item.getItemId()) {
		case R.id.refresh:

			KisiAPI.getInstance().refresh(new OnPlaceChangedListener() {

				@Override
				public void onPlaceChanged(Place[] newPlaces) {

					
				}

			});
			return true;

		case R.id.share:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.share_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}

			place = mLockListAdapter.getPlace();
			// check if user is owner
			if (!KisiAPI.getInstance().userIsOwner(place)) {
				Toast.makeText(this, R.string.share_owner_only, Toast.LENGTH_LONG).show();
				return false;
			}

			Intent intent = new Intent(getApplicationContext(), ShareKeyActivity.class);
			intent.putExtra("place", place.getId());
			startActivity(intent);
			return true;

		case R.id.showLog:
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.log_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}
			place = mLockListAdapter.getPlace();
			Intent logView = new Intent(getApplicationContext(), LogInfo.class);
			logView.putExtra("place_id", place.getId());
			startActivity(logView);

			return true;

		case R.id.setup:

			BlinkupController blinkup = BlinkupController.getInstance();
			blinkup.intentBlinkupComplete = new Intent(this, BlinkupCompleteActivity.class);

			if (KisiAPI.getInstance().getUser().getEiPlanId() != null) {
				blinkup.setPlanID(KisiAPI.getInstance().getUser().getEiPlanId());
			}
			blinkup.selectWifiAndSetupDevice(this, IMP_API_KEY, new ServerErrorHandler() {
						@Override
						public void onError(String errorMsg) {
							Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
						}
					});
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
			
			
		case R.id.notification:
			Intent settingsIntent = new Intent(this, PlaceNotificationSettings.class);
			startActivity(settingsIntent);
			return true;

		default:
			return false;
		}
	}
	
	public void refreshViews() {
		mDrawerListAdapter.notifyDataSetChanged();
		mDrawerLayout.invalidate();
		if(mLockListAdapter != null) {
			mLockListAdapter.notifyDataSetChanged();
			mLockList.invalidate();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settings, menu);
	    return true;
	}
	
	
	
	
	
	private void handleUnlockIntent(Intent intent) {
		if (intent.getExtras() != null)
			if (intent.getStringExtra("Type").equals("unlock")) {
				int placeId = intent.getIntExtra("Place", -1);
				for (int j = 0; j < KisiAPI.getInstance().getPlaces().length; j++) {
					if (KisiAPI.getInstance().getPlaces()[j].getId() == placeId) {
						selectItem(j, placeId);
						int lockId = intent.getIntExtra("Lock", -1);
						int mActivePosition = mLockListAdapter.getItemPosition(lockId);
						mLockList.performItemClick(mLockList.getAdapter().getView(mActivePosition, null, null), mActivePosition,
						        mLockList.getAdapter().getItemId(mActivePosition));
					}
				}

			}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE) {
			if (resultCode == AccountPickerActivity.LOGIN_FAILED) {
				finish();
				return;
			}
			if (resultCode == AccountPickerActivity.LOGIN_SUCCESS) {
				//start an update of the places
				KisiAPI.getInstance().refresh(new OnPlaceChangedListener() {
					@Override
					public void onPlaceChanged(Place[] newPlaces) {
						setUiIntoStartState();
					}
				});
				return;
			}
		} else {
			BlinkupController.getInstance().handleActivityResult(this,
					requestCode, resultCode, data);
		}
	}

	private void logout() {
		KisiAPI.getInstance().logout();
		finish();
	}

	@Override
	public void onPlaceChanged(Place[] newPlaces) {
		refreshViews();
	}
	
}
