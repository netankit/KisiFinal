package de.kisi.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.BaseActivity;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

public class KisiMainActivity extends BaseActivity implements OnPlaceChangedListener{
    
	public static final String IMP_API_KEY = "08a6dd6db0cd365513df881568c47a1c";
	public static final String NEW_RELIC_API_KEY = "AAe80044cf73854b68f6e83881c9e61c0df9d92e56";
	
	
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerListAdapter mDrawerListAdapter;
    private ListView mLockList;
    private LockListAdapter mLockListAdapter; 
    private ActionBarDrawerToggle mDrawerToggle;

    private MergeAdapter  mMergeAdapter;
    
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

		mMergeAdapter = new MergeAdapter();
		mDrawerListAdapter = new DrawerListAdapter(this);
		mMergeAdapter.addAdapter(mDrawerListAdapter);
		
		
		buildStaticMenuItems();
		
		
		
		mDrawerList.setAdapter(mMergeAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);	
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		
		mDrawerToggle = new ActionBarDrawerToggle( this,  mDrawerLayout, R.drawable.ic_drawer,  R.string.place_overview,  R.string.kisi ) {
	            public void onDrawerClosed(View view) {
	            	super.onDrawerClosed(view);
//	            	getActionBar().setTitle(getResources().getString(R.string.kisi));
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	                super.onDrawerOpened(drawerView);
	                getActionBar().setTitle(getResources().getString(R.string.place_overview));
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
		 };
		 
		mDrawerLayout.setDrawerListener(mDrawerToggle);	 
		 
		KisiAPI.getInstance().registerOnPlaceChangedListener(this);
		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);
	}
	
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(getIntent().hasExtra("Type")) {
			handleUnlockIntent(getIntent());
			getIntent().removeCategory("Type");
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(intent.hasExtra("Type")) {
			handleUnlockIntent(intent);
			intent.removeCategory("Type");
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
		getActionBar().setTitle(KisiAPI.getInstance().getPlaceById((int) id).getName());
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

		default:
			return super.onOptionsItemSelected(item);
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
						//check if there is a lockId in the intent and then unlock the right lock
						if(lockId != -1) {
							int mActivePosition = mLockListAdapter.getItemPosition(lockId);
							mLockList.performItemClick(mLockList.getAdapter().getView(mActivePosition, null, null), mActivePosition,
						        mLockList.getAdapter().getItemId(mActivePosition));
						}
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

		
	private void buildStaticMenuItems() {
	
		StaticMenuOnClickListener listener =  new StaticMenuOnClickListener(this);
		
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View divider =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider);
		
		TextView refreshButton = (TextView) li.inflate(R.layout.drawer_list_item, null);
		refreshButton.setId(R.id.refreshButton);
		refreshButton.setText(getResources().getText(R.string.refresh));
		refreshButton.setClickable(true);
		refreshButton.setOnClickListener(listener);
		mMergeAdapter.addView(refreshButton);
		
		TextView setup_kisi_button = (TextView) li.inflate(R.layout.drawer_list_item, null);
		setup_kisi_button.setId(R.id.setup_kisi_button);
		setup_kisi_button.setText(getResources().getText(R.string.setup));
		setup_kisi_button.setClickable(true);
		setup_kisi_button.setOnClickListener(listener);
		mMergeAdapter.addView(setup_kisi_button);
		
		TextView about = (TextView) li.inflate(R.layout.drawer_list_item, null);
		about.setId(R.id.about_button);
		about.setText(getResources().getText(R.string.about));
		about.setClickable(true);
		about.setOnClickListener(listener);
		mMergeAdapter.addView(about);
		
		TextView notification = (TextView) li.inflate(R.layout.drawer_list_item, null);
		notification.setId(R.id.notification_settings_button);
		notification.setText(getResources().getText(R.string.notification_settings));
		notification.setClickable(true);
		notification.setOnClickListener(listener);
		mMergeAdapter.addView(notification);
		
		TextView logout = (TextView) li.inflate(R.layout.drawer_list_item, null);	
		logout.setId(R.id.logout_button);
		notification.setText(getResources().getText(R.string.logout));
		notification.setClickable(true);
		notification.setOnClickListener(listener);
		mMergeAdapter.addView(logout);

	}
	
	
	
	
	
	
	
	
	private  class StaticMenuOnClickListener implements OnClickListener {

		private Activity activity;
			
		public StaticMenuOnClickListener(Activity activity){
			this.activity = activity;
		}
		
		@Override
		public void onClick(View v) {
			TextView textView =  (TextView) v;
			
			
			
			switch(v.getId()) {
				case R.id.refreshButton:
					KisiAPI.getInstance().refresh(new OnPlaceChangedListener() {
		
						@Override
						public void onPlaceChanged(Place[] newPlaces) {
		
							
						}
		
					});
				return;
			

			case R.id.setup_kisi_button:

				BlinkupController blinkup = BlinkupController.getInstance();
				blinkup.intentBlinkupComplete = new Intent(activity, BlinkupCompleteActivity.class);

				if (KisiAPI.getInstance().getUser().getEiPlanId() != null) {
					blinkup.setPlanID(KisiAPI.getInstance().getUser().getEiPlanId());
				}
				blinkup.selectWifiAndSetupDevice(activity, KisiMainActivity.IMP_API_KEY, new ServerErrorHandler() {
							@Override
							public void onError(String errorMsg) {
								Toast.makeText(KisiApplication.getInstance(), errorMsg, Toast.LENGTH_SHORT).show();
							}
						});
				return ;

			case R.id.logout_button:
				logout();
				return;
				
				
			case R.id.about_button:
				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setTitle(R.string.kisi);
				String versionName = null;
				try {
					versionName = KisiApplication.getInstance().getPackageManager().getPackageInfo(KisiApplication.getInstance().getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				alertDialog.setMessage(getResources().getString(R.string.version) + versionName);
				alertDialog.show();
				return;
				
				
			case R.id.notification_settings_button:
				Intent settingsIntent = new Intent(activity, PlaceNotificationSettings.class);
				startActivity(settingsIntent);
				return;
				

			default:
				return;
			}
		}

	}
	
	

	}
