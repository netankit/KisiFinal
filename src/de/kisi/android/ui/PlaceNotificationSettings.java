package de.kisi.android.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;


public class PlaceNotificationSettings extends Activity implements OnClickListener {

	private ListView mListView;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(getResources().getString(R.string.notification_settings));
		setContentView(R.layout.place_notification_settings);
		mListView = (ListView) findViewById(R.id.place_notification_listview);
		mListView.setAdapter(new PlaceNotificationAdapter(this));
//		buildShareDialog();
	}

	
	
	
	class PlaceNotificationAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		private Context context;
		
		public PlaceNotificationAdapter(Context context) {
			super();
			this.context = context;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return KisiAPI.getInstance().getPlaces().length;
		}

		@Override
		public Place getItem(int position) {
			return KisiAPI.getInstance().getPlaceAt(position);
		}

		@Override
		public long getItemId(int position) {
			return KisiAPI.getInstance().getPlaceAt(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
	        if (vi == null)
	            vi = inflater.inflate(R.layout.place_notification_item, null);
	        final Place p = this.getItem(position);
	        TextView placeName = (TextView) vi.findViewById(R.id.place_name_notification);
	        placeName.setText(p.getName());
	        TextView defaultSetting = (TextView) vi.findViewById(R.id.notification_default_setting);
	        defaultSetting.setText(context.getResources().getString(R.string.default_settings) + ": " +
	        		Boolean.toString(p.isSuggestUnlock()));
	        Switch placeSwitch = (Switch) vi.findViewById(R.id.place_switch_notification);
	        placeSwitch.setChecked(p.getNotificationEnabled());
	        placeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					p.setNotificationEnabled(isChecked);
				} 
				
			});
	        return vi;
		}
		
		
		
	}
	
	
	
//	private void buildShareDialog() {
//		final Place[] places = KisiAPI.getInstance().getPlaces();
//
//		// Getting px form Scale-independent Pixels
//		Resources r = getResources();
//		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
//				350, r.getDisplayMetrics());
//		int height = (int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_SP, 50, r.getDisplayMetrics());
//
//		
//		Typeface font = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "Roboto-Light.ttf");
//		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.place_linear_layout);
//		
//		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		
//		for (final Place p : places) {
//			
//			final Switch placeSwitch =  (Switch) li.inflate(R.layout.place_notification_switch, null); 
//			placeSwitch.setText(p.getName() + "\t");
//			placeSwitch.setChecked(p.getNotificationEnabled());
//			placeSwitch.setTypeface(font);
//			placeSwitch.setGravity(Gravity.LEFT);
//			placeSwitch.setHeight(height);
//			placeSwitch.setTextColor(Color.DKGRAY);
//			placeSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//			placeSwitch.setVisibility(View.VISIBLE);
//			linearLayout.addView(placeSwitch);
//			placeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//					p.setNotificationEnabled(isChecked);
//				} 
//				
//			});
//
//		}
//
//	}
	
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