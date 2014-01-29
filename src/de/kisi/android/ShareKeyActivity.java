package de.kisi.android;

import java.util.ArrayList;
import java.util.List;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShareKeyActivity extends Activity implements OnClickListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.share_key_activity);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.log_title);
		

		ImageButton backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(this);

		int place = getIntent().getExtras().getInt("place");
		buildShareDialog(KisiAPI.getInstance().getPlaces()[place]);

	}	
	
	
	private void buildShareDialog(final Place place) {
		final List<Lock> locks = place.getLocks();
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.place_linear_layout);
		
		final Drawable uncheckedIcon = getResources().getDrawable(R.drawable.share_unchecked);
		final Drawable checkedIcon = getResources().getDrawable(R.drawable.share_checked);
		final List<Lock> sendlocks = new ArrayList<Lock>();
		
		Typeface font = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "Roboto-Light.ttf");
		//Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());

		LinearLayout.LayoutParams layoutParams;
		layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(margin, margin, margin, margin);

		final TextView textTitle = new TextView(this);
		textTitle.setText(getResources().getString(R.string.share_title) + " " + place.getName());
		textTitle.setTextSize(24);
		textTitle.setTextColor(0xFFFFFFFF);
		linearLayout.addView(textTitle, layoutParams);
		final TextView textEmail = new TextView(this);
		textEmail.setText(getResources().getString(R.string.share_popup_msg));
		textEmail.setTextColor(0xFFFFFFFF);
		linearLayout.addView(textEmail, layoutParams);
		final EditText emailInput = new EditText(this);
		emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailInput.setHint(R.string.Email);
		emailInput.setTextColor(0xFFFFFFFF);
		emailInput.setHintTextColor(0xAAAAAA);
		linearLayout.addView(emailInput, layoutParams);
		
		layoutParams = new LinearLayout.LayoutParams(width, height);
		layoutParams.setMargins(margin, margin, margin, margin);
		for (final Lock lock : locks) {
			
			final Button button = new Button(this);
			button.setText(lock.getName());
			button.setTypeface(font);
			button.setGravity(Gravity.CENTER);
			button.setWidth(width);
			button.setHeight(height);
			button.setTextColor(Color.WHITE);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon, null, null, null);
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(new OnClickListener(){
				private boolean checked = false;
				@Override
				public void onClick(View v) {
					if (checked){
						button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon, null, null, null);
						sendlocks.remove(lock);
						checked = false;
					}else{
						button.setCompoundDrawablesWithIntrinsicBounds(checkedIcon, null, null, null);
						sendlocks.add(lock);
						checked = true;
					}
				}});
			
			linearLayout.addView(button, layoutParams);
		}
		Button submit = new Button(this);
		submit.setText(getResources().getString(R.string.share_submit_button));
		submit.setTextColor(0xFFFFFFFF);
		submit.setTextSize(25);
		layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(margin, margin, margin, margin);
		final Activity activity = this;
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
				KisiAPI.getInstance().createNewKey(place, email, sendlocks, activity);
				finish();
			}
		});
		linearLayout.addView(submit, layoutParams);

	}


	@Override
	public void onClick(View v) {
		finish();
	}
}
