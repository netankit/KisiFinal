package de.kisi.android.demoaccount;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.ui.KisiMainActivity;

public class DemoAccountActivity extends AccountAuthenticatorActivity implements OnClickListener, LoginCallback {

	private EditText firstNameField;
	private EditText lastNameField;
	private EditText emailField;
	private Button loginButton;
	private ProgressDialog progressDialog;
	
	private String username;
	private String password;
	
	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String PARAM_USER_PASS = "USER_PASS";
	public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_account_activity);
		
		firstNameField = (EditText) findViewById(R.id.firstName);
		lastNameField = (EditText) findViewById(R.id.lastName);
		emailField = (EditText) findViewById(R.id.email);
		loginButton = (Button) findViewById(R.id.backToLoginButton);
		loginButton.setOnClickListener(this);
		
		Button goToDemoAccountButton = (Button) findViewById(R.id.goToDemoAccountButton);
		goToDemoAccountButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backToLoginButton:
			finish();
			break;
		case R.id.goToDemoAccountButton:
			demo();
			break;
		default:
			break;
		}
		
	}
	
	private void demo() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.login_loading_message));
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		username = getString(R.string.demo_email);
		password = getString(R.string.demo_password);
	
        KisiAPI.getInstance().login(username, password, this);
	}

	

	@Override
	public void onLoginSuccess(String authToken) {
		progressDialog.dismiss();
		
		Intent main = new Intent(this, KisiMainActivity.class);
		startActivity(main);
    }

	@Override
	public void onLoginFail(String errormessage) {
		progressDialog.dismiss();
		Toast.makeText(getBaseContext(), errormessage, Toast.LENGTH_SHORT).show();	
	}

}
