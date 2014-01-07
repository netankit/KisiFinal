package de.kisi.android;

import java.util.List;
import java.util.Vector;

import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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



public class KisiMain extends FragmentActivity implements
		PopupMenu.OnMenuItemClickListener {
	
    private static final String API_KEY = "08a6dd6db0cd365513df881568c47a1c";

    private ViewPager pager;
	private Activity activity;
    private KisiAPI kisiAPI;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		kisiAPI = KisiAPI.getInstance();
		activity = this;
		
		if(KisiAccountManager.getInstance().getPassword() == null) {
			Intent login = new Intent(this,  LoginActivity.class);
			startActivity(login);
		}
		else {
			kisiAPI.login(KisiAccountManager.getInstance().getUserName(), KisiAccountManager.getInstance().getPassword(), new LoginCallback() {
				@Override
				public void onLoginSuccess(String authtoken) {					
					return;
				}
				
				@Override
				public void onLoginFail(String errormessage) {
					Intent login = new Intent(activity, LoginActivity.class);
					startActivity(login);
				}
			});
		}
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.kisi_main);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.window_title);
		
		pager = (ViewPager) findViewById(R.id.pager);
		
	}
    
    
    @Override
    public void onStart() {
    	super.onStart();
		setupView(kisiAPI.getPlaces());
		
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
	public boolean onMenuItemClick(MenuItem item) {
		// get all places
		Place[] places = kisiAPI.getPlaces();

		switch(item.getItemId())
		{
			case R.id.refresh:
				kisiAPI.refresh(new OnPlaceChangedListener() {

					@Override
					public void onPlaceChanged(Place[] newPlaces) {
						setupView(newPlaces );
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
				if (!kisiAPI.userIsOwner(p)){
					Toast.makeText(this, R.string.share_owner_only, Toast.LENGTH_LONG).show();
					return false;
				}
					
				Intent intent = new Intent(getApplicationContext(),ShareKeyActivity.class);
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

				SharedPreferences settings = getSharedPreferences("Config", MODE_PRIVATE);
				
				if(kisiAPI.getUser().getEiPlanId() != null)
					blinkup.setPlanID(settings.getString("ei_plan_id", null));
				
				blinkup.selectWifiAndSetupDevice(this, API_KEY, new ServerErrorHandler() {
			        @Override
			        public void onError(String errorMsg) {
			            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
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
	
	
	//callback for blinkup 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	BlinkupController.getInstance().handleActivityResult(this, requestCode, resultCode, data);
    }

	private void logout() {
		KisiAccountManager.getInstance().removeAccount();
		kisiAPI.logout();
		finish();
	}

	public void onBackPressed() {
		 moveTaskToBack(true);
	}

	@Override
	public  void onDestroy () {
		super.onDestroy();
		logout();
	}

	private void setupView(Place[] places) {

		List<Fragment> fragments = new Vector<Fragment>();
		
		for(int j=0;j<places.length;j++)
			fragments.add(PlaceFragment.newInstance(j));

		FragmentManager fm = getSupportFragmentManager();
		PlaceFragmentPagerAdapter pagerAdapter = new PlaceFragmentPagerAdapter(fm, fragments);
		pager.setAdapter(pagerAdapter);
	}
}
