package de.kisi.android.account;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.RegisterCallback;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener, RegisterCallback{
	
	private EditText userNameField;
	private EditText passwordField;
	private EditText passwordConfirmField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		userNameField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordConfirmField = (EditText) findViewById(R.id.passwordConfirm);
		
		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.registerButton:
			String username = userNameField.getText().toString();
			String password = passwordField.getText().toString();
			String passwordConfirm = passwordConfirmField.getText().toString();
			if(username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()){
				Toast.makeText(getApplicationContext(), R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
				break;
			}
			if(!password.equals(passwordConfirm)){
				Toast.makeText(getApplicationContext(), R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
				break;
			}
			// TODO Change true to the value from user interface
			KisiAPI.getInstance().register(username, password, passwordConfirm, true, this);
			
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onRegisterSuccess(String successmessage) {
		// TODO Show the success message and return the previous activity.
		Toast.makeText(getBaseContext(), successmessage, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRegisterFail(String errormessage) {
		Toast.makeText(getBaseContext(), errormessage, Toast.LENGTH_SHORT).show();
	}


}
