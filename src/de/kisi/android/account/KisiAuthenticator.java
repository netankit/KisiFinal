package de.kisi.android.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class KisiAuthenticator extends AbstractAccountAuthenticator {
	
	//TODO: maybe add later more
	 public static final String AUTHTOKEN_TYPE_DEFAULT = "kisidefault";
	 public static final String ACCOUNT_TYPE = "de.kisi";
	
	private final Context mContext;
	
	
	public KisiAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {

		final Intent intent = new Intent(mContext, AccountActivity.class);
		intent.putExtra(AccountActivity.ARG_ACCOUNT_TYPE, accountType);
		intent.putExtra(AccountActivity.ARG_AUTH_TYPE, authTokenType);
		intent.putExtra(AccountActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
//		 // Extract the user name and password from the Account Manager, and ask
//        // the server for an appropriate AuthToken.
//        final AccountManager am = AccountManager.get(mContext);
//
//        String authToken = am.peekAuthToken(account, authTokenType);
//
//        Log.d("getAuthToken", "> peekAuthToken returned - " + authToken);
//
//        // Lets give another try to authenticate the user
//        if (TextUtils.isEmpty(authToken)) {
//            final String password = am.getPassword(account);
//            if (password != null) {
//                try {
//                    //TODO: refactor this
//                	Log.d("udinic", "> re-authenticating with the existing password");
//                    JSONObject login_data = new JSONObject();
//            		JSONObject login_user = new JSONObject();
//            		try {
//            			login_data.put("email", account.name);
//            			login_data.put("password", password);
//            			login_user.put("user", login_data);
//            		} catch (JSONException e1) {
//            			e1.printStackTrace();
//            		}
//                    authToken = KisiRestClient.getInstance().signIn("users/sign_in", login_user);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        // If we get an authToken - we return it
//        if (!TextUtils.isEmpty(authToken)) {
//            final Bundle result = new Bundle();
//            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
//            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
//            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
//            return result;
//        }
//
//        // If we get here, then we couldn't access the user's password - so we
//        // need to re-prompt them for their credentials. We do that by creating
//        // an intent to display our AuthenticatorActivity.
//        final Intent intent = new Intent(mContext, AccountActivity.class);
//        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
//        intent.putExtra(AccountActivity.ARG_ACCOUNT_TYPE, account.type);
//        intent.putExtra(AccountActivity.ARG_AUTH_TYPE, authTokenType);
//        intent.putExtra(AccountActivity.ARG_ACCOUNT_NAME, account.name);
//        final Bundle bundle = new Bundle();
//        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
//        return bundle;
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

}
