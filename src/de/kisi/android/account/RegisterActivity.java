package de.kisi.android.account;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.RegisterCallback;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener, RegisterCallback{
	
	private EditText userNameField;
	private EditText passwordField;
	private EditText passwordConfirmField;
	private CheckBox agreedTermsField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		userNameField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordConfirmField = (EditText) findViewById(R.id.passwordConfirm);
		agreedTermsField = (CheckBox) findViewById(R.id.termsCondition);
		
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
			boolean termsAgreed = agreedTermsField.isChecked();
			
			if(!termsAgreed){
				Toast.makeText(getApplicationContext(), R.string.terms_and_conditions_not_agreed, Toast.LENGTH_SHORT).show();
				return;
			}
			if(username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()){
				Toast.makeText(getApplicationContext(), R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
				return;
			}
			if(!password.equals(passwordConfirm)){
				Toast.makeText(getApplicationContext(), R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
				return;
			}
			KisiAPI.getInstance().register(username, password, passwordConfirm, termsAgreed, this);
			
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onRegisterSuccess() {
		Toast.makeText(getBaseContext(), R.string.registration_successful, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRegisterFail(String errormessage) {
		Toast.makeText(getBaseContext(), errormessage, Toast.LENGTH_SHORT).show();
	}


}
