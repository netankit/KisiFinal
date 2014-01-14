package de.kisi.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class KisiAccountManager {
	private static KisiAccountManager instance;
	
	//TODO: put this into strings.xml
	private static final String ACCOUNT_TYPE =  "de.kisi";
	
	private  AccountManager accManager;
	
	public static void initialize(Context context){
		instance = new KisiAccountManager(context);
	}
	
	
	private KisiAccountManager(Context context) {
		accManager = AccountManager.get(context);
	}
	
	public static KisiAccountManager getInstance() {
		return instance;
	}

	public void deleteAccountByName(String name ) {
		Account availableAccounts[] = accManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);
		Account result = null;

		for(Account acc: availableAccounts) {
			if(acc.name.equals(name)) {
				accManager.removeAccount(acc, null, null);
				return;
			}
		}
		return;
	}	
}
