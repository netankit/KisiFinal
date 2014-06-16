package de.kisi.android.account;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.takeatour.TakeATourActivity;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// see https://github.com/Udinic/AccountAuthenticator/blob/master/src/com/udinic/accounts_authenticator_example/authentication/AuthenticatorActivity.java

public class AccountActivity extends AccountAuthenticatorActivity implements OnClickListener, LoginCallback{

	private EditText userNameField;
	private EditText passwordField;
	private String username;
	private String password;
	
	private AccountAuthenticatorResponse response;
	
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
		Intent takeTour = new Intent(this, TakeATourActivity.class);
		startActivity(takeTour);
		//Log.d("AccountActivity", "entered onCreate");
		setContentView(R.layout.login_activity);

		mAccountManager = AccountManager.get(getBaseContext());
		
		String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
	    mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
		
	    if(mAuthTokenType == null){
        	mAuthTokenType = KisiAuthenticator.AUTHTOKEN_TYPE_DEFAULT;
        }
	    
		Button loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);

		userNameField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		passwordField.setTypeface(Typeface.DEFAULT);
		
		TextView slogan = (TextView) findViewById(R.id.Slogan);
		Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		slogan.setTypeface(font);
		

		TextView newUser = (TextView) findViewById(R.id.registerText);
		newUser.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView forgotPw = (TextView) findViewById(R.id.forgot);
		forgotPw.setMovementMethod(LinkMovementMethod.getInstance());
		
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            /*
             * Pass the new account back to the account manager
             */
            response = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        }

		
		
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
//		Intent takeTour = new Intent(this, TakeATourActivity.class);
//		startActivity(takeTour);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.login_loading_message));
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		username = userNameField.getText().toString();
		password = passwordField.getText().toString();

        KisiAPI.getInstance().login(username, password, this);
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
	
	

}
