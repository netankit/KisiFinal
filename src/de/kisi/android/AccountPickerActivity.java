package de.kisi.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.kisi.android.account.KisiAuthenticator;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;

public class AccountPickerActivity extends Activity implements LoginCallback{

    public static int LOGIN_FAILED = -1;
    public static int LOGIN_SUCCESS = 0;
	
	private AccountManager mAccountManager;
	private ProgressDialog progressDialog;
	private final Activity activity = this;
	private final LoginCallback mloginCallback = this;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.pick_account);
		
	//	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.log_title);
		mAccountManager = AccountManager.get(this);
		buildAccountDailog();
		
	}	
	
	//but the buildAccountDailog in onResum to avoid the when app is started and
	// than an account is added in the setting the app don't recognize it
	@Override
	public void onResume() {
		super.onResume();

	}
	
	
	private void buildAccountDailog() {
		
		Account availableAccounts[] = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);
		
		
		
		//if there is no account show the user the login screen
		if (availableAccounts.length == 0) {
			  addAccount();
			  return;
		 }
		// if there is just one account login into this account
		else if(availableAccounts.length == 1) {
			Account acc = availableAccounts[0];
			String password = mAccountManager.getPassword(acc);
			//show progress dialog
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.login_loading_message));
			progressDialog.setCancelable(false);
			progressDialog.show();
			KisiAPI.getInstance().login(acc.name, password, mloginCallback);
			return;
		}
		//if there are more then one account let the user choose the right one
		else {
			LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayout1);
			//clear db and auth token before maybe login a different account
			KisiAPI.getInstance().clearCache();
			linearLayout.removeAllViews();
			Typeface font = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "Roboto-Light.ttf");
			//Getting px form Scale-independent Pixels
			Resources r = getResources();
			int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 350, r.getDisplayMetrics());
			int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
			int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());
	
			LinearLayout.LayoutParams layoutParams;
			layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(margin, margin, margin, margin);
	
			final TextView textTitle = new TextView(this);
			textTitle.setText("Choose Account");
			textTitle.setTextSize(24);
			textTitle.setTextColor(0xFFFFFFFF);
			linearLayout.addView(textTitle, layoutParams);
			layoutParams = new LinearLayout.LayoutParams(width, height);
			layoutParams.setMargins(margin, margin, margin, margin);
			for (final Account acc : availableAccounts) {
				final String password = mAccountManager.getPassword(acc);
				final Button button = new Button(this);
				button.setText(acc.name);
				button.setTypeface(font);
				button.setGravity(Gravity.CENTER);
				button.setWidth(width);
				button.setHeight(height);
				button.setTextColor(Color.WHITE);
				button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
							progressDialog = new ProgressDialog(activity);
							progressDialog.setMessage(getString(R.string.login_loading_message));
							progressDialog.setCancelable(false);
							progressDialog.show();
							KisiAPI.getInstance().login(acc.name, password, mloginCallback);
						}
					});
				
				linearLayout.addView(button, layoutParams);
			}
		}
	}
	
	private void addAccount() {
		final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(KisiAuthenticator.ACCOUNT_TYPE, KisiAuthenticator.AUTHTOKEN_TYPE_DEFAULT, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();  
                    buildAccountDailog(); 

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, null);
	}
	
	@Override
	public void onBackPressed() {
		progressDialog.dismiss();
		setResult(LOGIN_FAILED);
		finish();
	}
	
	
	@Override
	public void onLoginSuccess(String authtoken) {
		// Switch the activity to internal Views
		progressDialog.dismiss();
		//Intent mainScreen = new Intent(getApplicationContext(), KisiMain.class);
		//mainScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//finish login activity
		setResult(LOGIN_SUCCESS);
		finish();
	}

	@Override
	public void onLoginFail(String errormessage) {
		progressDialog.dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errormessage)
               .setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   buildAccountDailog();
                   }
               })
               .setNegativeButton(getString(R.string.close_app), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   setResult(LOGIN_FAILED);
                	   finish();
                   }
               });
        // Create the AlertDialog object and return it
        builder.show();
	}

	
}
