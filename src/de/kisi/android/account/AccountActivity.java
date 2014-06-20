package de.kisi.android.account;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// see https://github.com/Udinic/AccountAuthenticator/blob/master/src/com/udinic/accounts_authenticator_example/authentication/AuthenticatorActivity.java

public class AccountActivity extends AccountAuthenticatorActivity implements OnClickListener, LoginCallback, TextWatcher{

	private EditText userNameField;
	private EditText passwordField;
	private String username;
	private String password;
	private Button loginButton;
	
	private ProgressDialog progressDialog;
	
	//TODO:clean this up later. copy and paste code 
	private final int REQ_SIGNUP = 1;
	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
	public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
	public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

	public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
	
	public final static String PARAM_USER_PASS = "USER_PASS";
	
	private AccountManager mAccountManager;
	private String mAuthTokenType;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d("AccountActivity", "entered onCreate");
		setContentView(R.layout.login_activity);

		mAccountManager = AccountManager.get(getBaseContext());
		
	    mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
		
	    if(mAuthTokenType == null){
        	mAuthTokenType = KisiAuthenticator.AUTHTOKEN_TYPE_DEFAULT;
        }
	    
		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		TextView registerButton = (TextView) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(this);
		
		userNameField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordField.setTypeface(Typeface.DEFAULT);
		
		userNameField.addTextChangedListener(this);
		passwordField.addTextChangedListener(this);
		
		TextView slogan = (TextView) findViewById(R.id.Slogan);
		Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		slogan.setTypeface(font);
		
		TextView forgotPw = (TextView) findViewById(R.id.forgot);
		forgotPw.setMovementMethod(LinkMovementMethod.getInstance());
		
		
		
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.login_loading_message));
			progressDialog.setCancelable(false);
			progressDialog.show();
			
			username = userNameField.getText().toString();
			password = passwordField.getText().toString();
		
	        KisiAPI.getInstance().login(username, password, this);
			break;
		case R.id.registerButton:
			Intent login = new Intent(this, RegisterActivity.class);
			startActivityForResult(login, REQ_SIGNUP);
			break;
		default:
			break;
		}
	}

    private void finishLogin(Intent intent) {

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;
            
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

	@Override
	public void onLoginSuccess(String authToken) {
		progressDialog.dismiss();
		final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
		Bundle data = new Bundle();

		data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
		data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
		data.putString(PARAM_USER_PASS, password);

		final Intent res = new Intent();
		res.putExtras(data);
		finishLogin(res);
    }

	@Override
	public void onLoginFail(String errormessage) {
		progressDialog.dismiss();
		Toast.makeText(getBaseContext(), errormessage, Toast.LENGTH_SHORT).show();	
	}
	
	

	@Override
	public void afterTextChanged(Editable s) {
		username = userNameField.getText().toString();
		password = passwordField.getText().toString();
		
		if(!username.isEmpty() && !password.isEmpty())
			loginButton.setEnabled(true);
		else
			loginButton.setEnabled(false);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}
}
