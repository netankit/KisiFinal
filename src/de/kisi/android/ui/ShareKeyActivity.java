package de.kisi.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class ShareKeyActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.share_key_activity);
		
		int placeId = getIntent().getExtras().getInt("place");
		buildShareDialog(KisiAPI.getInstance().getPlaceById(placeId));
		//add back button to action bar 
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}	
	
	
	private void buildShareDialog(final Place place) {
		final List<Lock> locks = place.getLocks();
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.place_linear_layout);
		
		final List<Lock> sendlocks = new ArrayList<Lock>();
		
		//Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());

		LinearLayout.LayoutParams layoutParams;
		final EditText emailInput = (EditText) findViewById(R.id.shareEmailInput);
		
		layoutParams = new LinearLayout.LayoutParams(width, height);
		layoutParams.setMargins(margin, margin, margin, margin);
		for (final Lock lock : locks) {
			
			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			CheckBox checkBox = (CheckBox) li.inflate(R.layout.share_checkbox, null);
			checkBox.setText(lock.getName());	
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						sendlocks.add(lock);
					}else{
						sendlocks.remove(lock);
					}
				}
				
			});
//			final Button button = new Button(this);
//			button.setText(lock.getName());
//			button.setTypeface(font);
//			button.setGravity(Gravity.CENTER);
//			button.setWidth(width);
//			button.setHeight(height);
//			button.setTextColor(Color.DKGRAY);
//			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
//			button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon, null, null, null);
//			button.setVisibility(View.VISIBLE);
//			button.setOnClickListener(new OnClickListener(){
//				private boolean checked = false;
//				@Override
//				public void onClick(View v) {
//					if (checked){
//						button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon, null, null, null);
//						sendlocks.remove(lock);
//						checked = false;
//					}else{
//						button.setCompoundDrawablesWithIntrinsicBounds(checkedIcon, null, null, null);
//						sendlocks.add(lock);
//						checked = true;
//					}
//				}});
//			
//			linearLayout.addView(button, layoutParams);
			linearLayout.addView(checkBox, layoutParams);
		}
		
		Button submit = new Button(this);
		submit.setText(getResources().getString(R.string.share_submit_button));
		submit.setTextColor(Color.DKGRAY);
		submit.setTextSize(25);
		layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(margin, margin, margin, margin);
		submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(sendlocks.isEmpty()){
					Toast.makeText(getApplicationContext(), R.string.share_error, Toast.LENGTH_LONG).show();
					return;
				}
				String email = emailInput.getText().toString();
				if(email.isEmpty()) {
					Toast.makeText(getApplicationContext(), R.string.share_error_empty_email, Toast.LENGTH_LONG).show();
					return;
				}
				KisiAPI.getInstance().createNewKey(place, email, sendlocks);
				finish();
			}
		});
		linearLayout.addView(submit, layoutParams);

	}
	
	//listener for the back button in the action bar
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

}
