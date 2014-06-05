package de.kisi.android.account;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.RegisterCallback;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
	
	private ProgressDialog progressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		emailField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordConfirmField = (EditText) findViewById(R.id.passwordConfirm);
		agreedTermsField = (CheckBox) findViewById(R.id.termsCondition);
		
		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(this);
		
		TextView readTermsAndConditions = (TextView) findViewById(R.id.termsConditionLink);
		readTermsAndConditions.setOnClickListener(this);
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
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.loading_message));
			progressDialog.setCancelable(false);
			progressDialog.show();
			KisiAPI.getInstance().register(email, password, termsAgreed, this);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.registerButton:
			register();
			break;
		case R.id.termsConditionLink:
			Intent termsViewIntent = new Intent(this, TermsAndConditionsActivity.class);
			startActivity(termsViewIntent);
			break;
		default:
			break;
		}
		
	}
	@Override
	public void onRegisterSuccess() {
		progressDialog.dismiss();
		emailField.setText("");
		passwordField.setText("");
		passwordConfirmField.setText("");
		agreedTermsField.setChecked(false);
		
		TextView errorText = (TextView) findViewById(R.id.ErrorMessage);
		errorText.setText("");
		errorText.setVisibility(View.GONE);
		
		Toast.makeText(getBaseContext(), R.string.registration_successful, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onRegisterFail(String errormessage) {
		progressDialog.dismiss();
		TextView errorText = (TextView) findViewById(R.id.ErrorMessage);
		errorText.setText(errormessage);
		errorText.setVisibility(View.VISIBLE);
		
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(50);
		anim.setStartOffset(90);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(5);
		errorText.startAnimation(anim);
	}
}
