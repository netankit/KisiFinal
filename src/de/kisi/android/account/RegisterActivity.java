package de.kisi.android.account;

import de.kisi.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener{
	
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
			String userName = userNameField.getText().toString();
			String password = passwordField.getText().toString();
			String passwordConfirm = passwordConfirmField.getText().toString();
			//TODO check if they are empty!!!
			if(!password.equals(passwordConfirm)){
				Toast toast = Toast.makeText(getApplicationContext(), R.string.passwords_not_match, Toast.LENGTH_SHORT);
				toast.show();
			}else{
				Toast toast = Toast.makeText(getApplicationContext(), userName + ":" + password + ":" + passwordConfirm, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			break;
		default:
			break;
		}
		
	}

}
