package de.kisi.android.ui;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;


public class PlaceNotificationSettings extends Activity implements OnClickListener {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);


//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.place_notification_settings);

//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.log_title);

		buildShareDialog();



	}

	private void buildShareDialog() {
		final Place[] places = KisiAPI.getInstance().getPlaces();

		// Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 50, r.getDisplayMetrics());

		
		Typeface font = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "Roboto-Light.ttf");
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.place_linear_layout);

		
//		TextView textView =  new TextView(this);
//		textView.setText(getResources().getString(R.string.notification_settings));
//		textView.setTextSize(24);
//		textView.setTextColor(Color.DKGRAY);
//		textView.setTypeface(font);
//		linearLayout.addView(textView, layoutParams);
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (final Place p : places) {
			
			final Switch placeSwitch =  (Switch) li.inflate(R.layout.place_notification_switch, null); 
//			final Switch placeSwitch = new Switch(this);
			placeSwitch.setText(p.getName());
			placeSwitch.setChecked(p.getNotificationEnabled());
			placeSwitch.setTypeface(font);
			placeSwitch.setGravity(Gravity.LEFT);
			placeSwitch.setHeight(height);
			placeSwitch.setTextColor(Color.DKGRAY);
			placeSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			placeSwitch.setVisibility(View.VISIBLE);
			linearLayout.addView(placeSwitch);
			placeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					p.setNotificationEnabled(isChecked);
				} 
				
			});

		}

	}
	
	//listener for the backbutton of the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}

	@Override
	public void onClick(View v) {
		finish();
	}


}