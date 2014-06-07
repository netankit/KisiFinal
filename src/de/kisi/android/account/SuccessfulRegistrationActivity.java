package de.kisi.android.account;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;
import de.kisi.android.R;
import android.view.View.OnClickListener;

public class SuccessfulRegistrationActivity extends Activity implements
		OnClickListener {
	private Button loginButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.successful_registration);

		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			// On the click of a button we start Login Activity
			Intent startLoginActivity = new Intent(this, AccountActivity.class);
			startActivity(startLoginActivity);
			break;

		default:
			break;
		}

	}
}
