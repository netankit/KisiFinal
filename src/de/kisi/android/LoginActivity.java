package de.kisi.android;

import com.newrelic.agent.android.NewRelic;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends Activity implements OnClickListener,LoginCallback {

	private EditText userNameField;
	private EditText passwordField;
	private CheckBox savePassword;

	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_activity);

		Button loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);

		userNameField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordField.setTypeface(Typeface.DEFAULT);

		savePassword = (CheckBox) findViewById(R.id.rememberCheckBox);

		TextView slogan = (TextView) findViewById(R.id.Slogan);
		Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		slogan.setTypeface(font);
		

		TextView newUser = (TextView) findViewById(R.id.registerText);
		newUser.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView forgotPw = (TextView) findViewById(R.id.forgot);
		forgotPw.setMovementMethod(LinkMovementMethod.getInstance());

//		NewRelic.withApplicationToken(
//				"AAe80044cf73854b68f6e83881c9e61c0df9d92e56"
//				).start(this.getApplication());

	}

	@Override
	public void onBackPressed() { 
		// sends App to background if back button is pressed
		// prevents security issues
		moveTaskToBack(true);
	}

	@Override
	protected void onStart() {
		super.onStart();

		settings = getSharedPreferences("Config", MODE_PRIVATE);
		String email = settings.getString("email", "");
		String password = settings.getString("password", "");

		if (!email.isEmpty()) {
			userNameField.setText(email);
			if (password.isEmpty()) {
				passwordField.requestFocus();
			} else {
				passwordField.setText(password);
				savePassword.setChecked(true);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// user touched the login botton: gather all informations and send to next view
		
		// clear auth token
		Editor editor = settings.edit();
		editor.remove("authentication_token");
		editor.commit();

		String email = userNameField.getText().toString();
		String password = passwordField.getText().toString();
		
		// test for complete login data
		if(email.contains("@") && !password.isEmpty()){
			KisiAPI.getInstance().login(email, password, this, this);
		}else{
			Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.missing_login_data), Toast.LENGTH_SHORT).show();
			return;
		}
		
	}


	@Override
	public void onLoginSuccess() {
		// Save login credentials
		Editor editor = settings.edit();
		editor.putString("email", userNameField.getText().toString());
		if (savePassword.isChecked())
			// TODO: never save the password on the phone
			// especially not in plain text
			editor.putString("password", passwordField.getText().toString());
		else
			editor.remove("password");;
		editor.commit();
		
		// Switch the activity to internal Views
		Intent mainScreen = new Intent(getApplicationContext(), KisiMain.class);
		startActivity(mainScreen);

	}

	@Override
	public void onLoginFail(String errormessage) {
		Toast.makeText(getApplicationContext(), errormessage, Toast.LENGTH_SHORT).show();
		passwordField.setText("");
	}


}
