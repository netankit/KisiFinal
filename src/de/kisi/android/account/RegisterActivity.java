package de.kisi.android.account;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener, RegisterCallback{
	
	private EditText emailField;
	private EditText passwordField;
	private EditText passwordConfirmField;
	private CheckBox agreedTermsField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		emailField = (EditText) findViewById(R.id.email);
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
			register();
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onRegisterSuccess() {
		Toast.makeText(getBaseContext(), R.string.registration_successful, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onRegisterFail(String errormessage) {
		Toast.makeText(getBaseContext(), errormessage, Toast.LENGTH_SHORT).show();
	}

	private void focusOnField(TextView field, String error){
		field.setError(error);			
	}
	private boolean validateEmail(String email) {
		Pattern pattern;
		Matcher matcher;
		String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}
	private void register(){
		String email = emailField.getText().toString();
		String password = passwordField.getText().toString();
		String passwordConfirm = passwordConfirmField.getText().toString();
		boolean termsAgreed = agreedTermsField.isChecked();
		
		boolean errorFlag = false;
		
		if(!validateEmail(email)){
			focusOnField(emailField, getResources().getString(R.string.email_not_valid));
			errorFlag = true;
		}
		if(!password.equals(passwordConfirm)){
			focusOnField(passwordField, getResources().getString(R.string.passwords_not_match));
			focusOnField(passwordConfirmField, getResources().getString(R.string.passwords_not_match));
			errorFlag = true;
		}else{
			if(password.isEmpty() || password.length() < 8){
				focusOnField(passwordField, getResources().getString(R.string.password_length_error));
				errorFlag = true;
			}
			if(passwordConfirm.isEmpty() || passwordConfirm.length() < 8){
				focusOnField(passwordConfirmField, getResources().getString(R.string.password_length_error));
				errorFlag = true;
			}
		}

		if(!termsAgreed){
			focusOnField(agreedTermsField, getResources().getString(R.string.terms_and_conditions_not_agreed));
			errorFlag = true;
		}
		if(!errorFlag){
			KisiAPI.getInstance().register(email, password, termsAgreed, this);
		}
	}
}
