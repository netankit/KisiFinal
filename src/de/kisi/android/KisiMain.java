package de.kisi.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.manavo.rest.RestCache;
import com.manavo.rest.RestCallback;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;

import com.newrelic.agent.android.NewRelic;



public class KisiMain extends FragmentActivity implements
		PopupMenu.OnMenuItemClickListener {
	
    private static final String API_KEY = "08a6dd6db0cd365513df881568c47a1c";
	

	private SparseArray<Place> places;
	private ViewPager pager;
	
	private BlinkupController blinkup;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getSharedPreferences("Config", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("toLog", false);
		editor.commit();


		// set custom window title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.kisi_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.window_title);

		pager = (ViewPager) findViewById(R.id.pager);

		updatePlaces();

		blinkup = BlinkupController.getInstance();
		blinkup.intentBlinkupComplete = new Intent(this, BlinkupCompleteActivity.class);
		
		NewRelic.withApplicationToken(
				"AAe80044cf73854b68f6e83881c9e61c0df9d92e56"
				).start(this.getApplication());
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
	public void onPause() { 
		// sends user back to Login Screen if he didn't choose remember me
		SharedPreferences settings = getSharedPreferences("Config",
				MODE_PRIVATE);
		if (!settings.getBoolean("saved", false)) {
			if(!settings.getBoolean("toLog", false)){
			Intent loginScreen = new Intent(getApplicationContext(),
					LoginActivity.class);
			startActivity(loginScreen);}
		}
		super.onPause();
	}

	private void updatePlaces() {
		KisiApi api = new KisiApi(this);
		api.setCachePolicy(RestCache.CachePolicy.CACHE_THEN_NETWORK);
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONArray data = (JSONArray) obj;

				setupView(data);
			}

		});
		api.get("places");

	}

	private void setupView(JSONArray locations_json) {

		List<Fragment> fragments = new Vector<Fragment>();
		places = new SparseArray<Place>();
		try {
			for (int i = 0, j = 0; i < locations_json.length(); i++) {
				Place location = new Place(locations_json.getJSONObject(i));
				// The API returned some locations twice, so let's check if we
				// already have it or not also check if the place has a locks 
				// otherwise just don't show it
				// this doesnt work until the backend supports it !!!!
				//if ((places.indexOfKey(location.getId()) < 0) && (!locations_json.getJSONObject(i).isNull("locks") )) {
				if (places.indexOfKey(location.getId()) < 0){	
					places.put(location.getId(), location);
					fragments.add(PlaceFragment.newInstance(j++));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FragmentManager fm = getSupportFragmentManager();
		PlaceFragmentPagerAdapter pagerAdapter = new PlaceFragmentPagerAdapter(fm,
				fragments, this);
		pager.setAdapter(pagerAdapter);
	}

	
	//error handler for BlinkUp 
    private ServerErrorHandler errorHandler = new ServerErrorHandler() {
        @Override
        public void onError(String errorMsg) {
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    };   
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// set up
		int id = item.getItemId();
		if (id == R.id.refresh) {
			RestCache.clear(this);
			updatePlaces();
			return true;
		} else if (id == R.id.share) {
			// check if user has a place
			if (places.size() == 0) {
				Toast.makeText(this, R.string.share_empty_place_error, Toast.LENGTH_LONG).show();
				return true;
			} else {

				Place p = places.valueAt(pager.getCurrentItem());

				if (p.getOwnerId() != KisiApi.getUserId()) {
					Toast.makeText(this, R.string.share_owner_only,
							Toast.LENGTH_LONG).show();
					return false;
				} else {
					// show view with form to select locks + assignee_email
					buildShareDialog(p);
					return true;
				}
			}
		} else if (id == R.id.showLog) {
			// check if user has a place
			if (places.size() == 0) {
				Toast.makeText(this, R.string.log_empty_place_error, Toast.LENGTH_LONG).show();
				return true;
			} else {
				{
					SharedPreferences settings = getSharedPreferences("Config",
							MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("toLog", true);
					editor.commit();
				}
				Place place = places.valueAt(pager.getCurrentItem());

				Intent logView = new Intent(getApplicationContext(),
						LogInfo.class);
				logView.putExtra("place_id", place.getId());
				startActivity(logView);

				return true;
			}
		} else if (id == R.id.setup) {
			SharedPreferences settings = getSharedPreferences("Config",
					MODE_PRIVATE);
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("toLog", true);
				editor.commit();
			}
			if(settings.contains("ei_plan_id"))
				blinkup.setPlanID(settings.getString("ei_plan_id", null));
			blinkup.selectWifiAndSetupDevice(this, API_KEY, errorHandler);
			return true;
		} else if (id == R.id.logout) {
			logout();
			return true;
		}
		return false;

	}
	
	
	//callback for blinkup 
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
    	blinkup.handleActivityResult(this, requestCode, resultCode, data);
    }
	
	

	private void logout() {
		KisiApi api = new KisiApi(this);
		final Activity activity = this;
		
		api.setLoadingMessage(R.string.logout_in_progress);
		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				SharedPreferences settings = getSharedPreferences("Config", MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor = settings.edit();
				editor.remove("authentication_token");
				editor.commit();
				
				Toast.makeText(activity, R.string.logout_successful, Toast.LENGTH_LONG).show();
				finish();
			}
		});
		api.delete("users/sign_out");
	}

	private void buildShareDialog(Place p) {
		final Place currentPlace = p;
		final List<Lock> locks = currentPlace.getLocks();
		LinearLayout linearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 0, 0, 20);
		linearLayout.setLayoutParams(layoutParams);

		linearLayout.setOrientation(LinearLayout.VERTICAL);

		final EditText emailInput = new EditText(this);
		emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailInput.setHint(R.string.Email);
		linearLayout.addView(emailInput, layoutParams);

		final List<CheckBox> checkList = new ArrayList<CheckBox>();
		for (Lock lock : locks) {
			CheckBox checkbox = new CheckBox(this);
			checkbox.setText(lock.getName());
			checkList.add(checkbox);
			linearLayout.addView(checkbox, layoutParams);
		}

		AlertDialog.Builder inputAlertDialog = new AlertDialog.Builder(this);
		inputAlertDialog.setView(linearLayout);
		inputAlertDialog.setTitle(getResources().getString(R.string.share_title) + " " + p.getName());
		inputAlertDialog.setMessage(R.string.share_popup_msg);

		inputAlertDialog.setPositiveButton(R.string.share_submit_button,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						String email = emailInput.getText().toString();

						List<Lock> sendlocks = new ArrayList<Lock>();
						for (int i = 0; i < checkList.size(); i++) {
							if (checkList.get(i).isChecked()) {
								sendlocks.add(locks.get(i));
							}
						}
						if(sendlocks.isEmpty()){
							Toast.makeText(getApplicationContext(), R.string.share_error, Toast.LENGTH_LONG).show();
							arg0.dismiss();
							return;
						}
						if(email.isEmpty()) {
							Toast.makeText(getApplicationContext(), R.string.share_error_empty_email, Toast.LENGTH_LONG).show();
							arg0.dismiss();
							return;
						}

						else if (createNewKey(currentPlace, email, sendlocks) == false) {
							arg0.dismiss();
						}
						arg0.dismiss();
					}

				});

		inputAlertDialog.show();
	}

	private boolean createNewKey(Place p, String email, List<Lock> locks) {

		KisiApi api = new KisiApi(this);

		JSONArray lock_ids = new JSONArray();
		for (Lock l : locks) {
			lock_ids.put(l.getId());
		}
		JSONObject key = new JSONObject();
		try {
			key.put("lock_ids", lock_ids);
			key.put("assignee_email", email);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		api.addParameter("key", (Object) key);

		final Activity activity = this;

		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONObject data = (JSONObject) obj;
				try {
					Toast.makeText(
							activity,
							String.format(getResources().getString(R.string.share_success),
								data.getString("assignee_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
		api.post("places/" + String.valueOf(p.getId()) + "/keys");
		return true;
	}
	
	//TODO: tk: I NEED this to go out of the Activity. Either the singletonizedª API class serves
	//as cache or some other new class takes over data handling.
	public SparseArray<Place> getPlaces() {
		if (places == null) {
			Log.d("KisiMain", "places is null");
			updatePlaces();
		}
		return places;
	}
}
