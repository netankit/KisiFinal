package de.kisi.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.electricimp.blinkup.BlinkupController;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.BaseActivity;
import de.kisi.android.R;
import de.kisi.android.account.KisiAuthenticator;
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
    private int selectedPosition = 0;
    
    private MergeAdapter  mMergeAdapter;
    
    
    private TextView accountName;
    
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
		mDrawerList.setFocusableInTouchMode(false);
		mMergeAdapter = new MergeAdapter();
		mDrawerListAdapter = new DrawerListAdapter(this);
		
		buildTopDivider();
		
		mMergeAdapter.addAdapter(mDrawerListAdapter);
		
		buildStaticMenuItems();
		
		mDrawerList.setAdapter(mMergeAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);	
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);		
		mDrawerToggle = new ActionBarDrawerToggle( this,  mDrawerLayout, R.drawable.ic_drawer,  R.string.place_overview,  R.string.kisi ) {
	            public void onDrawerClosed(View view) {
	            	super.onDrawerClosed(view);
	            	Place place = (Place)mDrawerListAdapter.getItem(selectedPosition);
	            	if (place !=null){
	            		String newTitle = place.getName();
	            		getActionBar().setTitle(newTitle);
	            	}
	            	invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }

	            public void onDrawerOpened(View drawerView) {
	                super.onDrawerOpened(drawerView);
	                getActionBar().setTitle(getResources().getString(R.string.place_overview));
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
		 };
		 
		mDrawerLayout.setDrawerListener(mDrawerToggle);	 
		
		
		AccountManager mAccountManager = AccountManager.get(this);
		if(KisiAPI.getInstance().getUser()==null){
			if(mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE).length==1){
				Account account = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE)[0];
				mAccountManager.removeAccount(account, null, null);
			}
			Intent login = new Intent(this, AccountPickerActivity.class);
			startActivityForResult(login, LOGIN_REQUEST_CODE);
		}
		else {
			setUiIntoStartState();
		}
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
			Log.d("Onstart", String.valueOf(mLockList.getCount()) );
			handleIntent(getIntent());
			getIntent().removeExtra("Type");
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(intent.hasExtra("Type")) {
			handleIntent(intent);
			intent.removeExtra("Type");
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
		accountName.setText(KisiAPI.getInstance().getUser() == null ?  " " : KisiAPI.getInstance().getUser().getEmail() );
		if(place != null) {
			// - 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
			selectItem(selectedPosition + 2, mDrawerListAdapter.getItemId(selectedPosition));
		}
		else {
			KisiAPI.getInstance().updatePlaces(this);
		}
	}
	
	
	private void selectItem(int position, long id) {
		Place place = KisiAPI.getInstance().getPlaceById((int) id);
		if(place!=null)
			getActionBar().setTitle(place.getName());
		// - 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
		selectedPosition = position - 2;
		mDrawerListAdapter.selectItem(selectedPosition);
		mLockListAdapter = new LockListAdapter(this, (int) id);
		mLockList.setAdapter(mLockListAdapter);
		mLockList.setOnItemClickListener(new LockListOnItemClickListener(place));
		mLockList.invalidate();
	    // Highlight the selected item, update the title, and close the drawer and check if the position is available 
		// + 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
		if( position < mDrawerListAdapter.getCount() + 2) {
			mDrawerList.setItemChecked(position, true);
		}
		else {
			mDrawerList.setItemChecked(2, true);
		}
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
		if(item.getItemId() == R.id.share || item.getItemId() == R.id.share_actionbar_button) {
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
		}
		else if(item.getItemId() == R.id.showLog) {
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
		}
			return super.onOptionsItemSelected(item);
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
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	
	
	private void handleIntent(Intent intent) {
		// No extras, nothing to do
		if (intent.getExtras() == null)
			return;
		String type = intent.getStringExtra("Type");
		if (type.equals("unlock"))
			handleUnlockIntent(intent);
		if (type.equals("highlight"))
			handleHighlightIntent(intent);
		if (type.equals("nfcNoLock"))
			handleNFCNoLockIntent();
	}

	private void handleNFCNoLockIntent(){
		AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.ok),null).create();
		alertDialog.setTitle(R.string.restricted_access);
		alertDialog.setMessage(getResources().getString(R.string.no_access_to_lock));
		alertDialog.show();
	}
	
	
	private void handleUnlockIntent(Intent intent) {
		String sender = intent.getStringExtra("Sender");
		Log.i("sender","sender: "+sender);
		if (intent.getExtras() != null){
			int placeId = intent.getIntExtra("Place", -1);
			for (int j = 0; j < KisiAPI.getInstance().getPlaces().length; j++) {
				if (KisiAPI.getInstance().getPlaces()[j].getId() == placeId) {
					selectItem(j, placeId);
					int lockId = intent.getIntExtra("Lock", -1);
					//check if there is a lockId in the intent and then unlock the right lock
					if(lockId != -1) {
						int mActivePosition = mLockListAdapter.getItemPosition(lockId);
						mLockListAdapter.setTrigger(sender);
						mLockList.invalidate();
						Log.d("handle intent", String.valueOf(mLockList.getCount()) );
						// + 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
						//TODO: review this code: +2 caused an indexOutOfBoundsException so i removed it 
						mLockList.performItemClick(mLockList.getAdapter().getView(mActivePosition, null, null), mActivePosition,
								mLockList.getAdapter().getItemId(mActivePosition));
					}
				}
			}
		}
	}
	
	private void handleHighlightIntent(Intent intent){
		if (intent.getExtras() != null){
			int placeId = intent.getIntExtra("Place", -1);
			for (int j = 0; j < KisiAPI.getInstance().getPlaces().length; j++) {
				if (KisiAPI.getInstance().getPlaces()[j].getId() == placeId) {
					selectItem(j, placeId);
					int lockId = intent.getIntExtra("Lock", -1);
					//check if there is a lockId in the intent and then unlock the right lock
					if(lockId != -1) {
						int mActivePosition = mLockListAdapter.getItemPosition(lockId);
						mLockListAdapter.addSuggestedNFC(lockId);
						mLockList.invalidate();
						Log.d("handle intent", String.valueOf(mLockList.getCount()) );
						Button highlightElement = (Button)mLockList.getAdapter().getView(mActivePosition, null, null);
						Log.i("nfc",highlightElement.getText().toString());
						highlightElement.setBackgroundColor(Color.BLACK);
					}
				}
			}
		}
	}

	// callback for blinkup and login
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE) {
			switch(resultCode){
				case AccountPickerActivity.LOGIN_FAILED:
					KisiAPI.getInstance().logout();
					Intent login = new Intent(this, AccountPickerActivity.class);
					startActivityForResult(login, LOGIN_REQUEST_CODE);
					return;

				case AccountPickerActivity.LOGIN_OPTIMISTIC_SUCCESS:
					Log.i("onActivityResult", "LOGIN_OPTIMISTIC_SUCCESS");
					KisiAPI.getInstance().updatePlaces(this);
					return;
				case AccountPickerActivity.LOGIN_REAL_SUCCESS:
					Log.i("onActivityResult", "LOGIN_REAL_SUCCESS");
					KisiAPI.getInstance().updatePlaces(this);
					setUiIntoStartState();
					return;

				case AccountPickerActivity.LOGIN_CANCELED:
					finish();
			}
		} else {
			BlinkupController.getInstance().handleActivityResult(this,requestCode, resultCode, data);
		}
	}

	
	
	public void logout() {
		KisiAPI.getInstance().logout();
		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);
	}

	@Override
	public void onPlaceChanged(Place[] newPlaces) {
		refreshViews();
		setUiIntoStartState();
	}

	
	//these methods fill the drawer with its static elements
	
	
	private void buildTopDivider() {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final TextView places = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		places.setText(getResources().getText(R.string.place_overview));
		mMergeAdapter.addView(places);
		
		final View divider =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider);
	}
	
	
	
		
	private void buildStaticMenuItems() {
		
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		final TextView settings = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		settings.setText(getResources().getText(R.string.settings));
		mMergeAdapter.addView(settings);
		
		final View divider =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider);
	
		StaticMenuOnClickListener listener =  new StaticMenuOnClickListener(this);
		
		final TextView refreshButton = (TextView) li.inflate(R.layout.drawer_list_item, null);
		refreshButton.setId(R.id.refreshButton);
		refreshButton.setText(getResources().getText(R.string.refresh));
		refreshButton.setClickable(true);
		refreshButton.setOnClickListener(listener);
		mMergeAdapter.addView(refreshButton);
		
		addDivider();
		
		final TextView setup_kisi_button = (TextView) li.inflate(R.layout.drawer_list_item, null);
		setup_kisi_button.setId(R.id.setup_kisi_button);
		setup_kisi_button.setText(getResources().getText(R.string.setup));
		setup_kisi_button.setClickable(true);
		setup_kisi_button.setOnClickListener(listener);
		mMergeAdapter.addView(setup_kisi_button);
		
		addDivider();
		
		final TextView notification = (TextView) li.inflate(R.layout.drawer_list_item, null);
		notification.setId(R.id.notification_settings_button);
		notification.setText(getResources().getText(R.string.notification_settings));
		notification.setClickable(true);
		notification.setOnClickListener(listener);
		mMergeAdapter.addView(notification);
		
		addDivider();
		
		final TextView about = (TextView) li.inflate(R.layout.drawer_list_item, null);
		about.setId(R.id.about_button);
		about.setText(getResources().getText(R.string.about));
		about.setClickable(true);
		about.setOnClickListener(listener);
		mMergeAdapter.addView(about);
		
		final TextView account = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		account.setText(getResources().getText(R.string.account));
		mMergeAdapter.addView(account);
		
		final View divider2 =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider2);
		
		
		accountName = (TextView) li.inflate(R.layout.drawer_list_item, null);
		accountName.setText(KisiAPI.getInstance().getUser() == null ?  " " : KisiAPI.getInstance().getUser().getEmail() );
		accountName.setTextColor(Color.GRAY);
		mMergeAdapter.addView(accountName);
		
		addDivider();
		
		final TextView logout = (TextView) li.inflate(R.layout.drawer_list_item, null);	
		logout.setId(R.id.logout_button);
		logout.setText(getResources().getText(R.string.logout));
		logout.setClickable(true);
		logout.setOnClickListener(listener);
		mMergeAdapter.addView(logout);
		
	
		

	}
	
	private void addDivider() {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View divider =  (View) li.inflate(R.layout.drawer_list_small_divider, null);
		mMergeAdapter.addView(divider);
	}
	

	}
