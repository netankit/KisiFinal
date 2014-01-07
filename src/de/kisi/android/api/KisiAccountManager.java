package de.kisi.android.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class KisiAccountManager {
	public static KisiAccountManager instance;
	
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
	
	public boolean addAccount(String email, String password) {
		Account account = new Account(email, ACCOUNT_TYPE);
		return accManager.addAccountExplicitly(account, password, null);
	}
	
	public String getUserName(){
		Account[] accounts = accManager.getAccountsByType(ACCOUNT_TYPE);
		if(accounts.length > 1 || accounts == null) {
			return null;
		}
		else {
			return accounts[0].name;
		}
	}
	
	
	public String getPassword() {
		Account[] accounts = accManager.getAccountsByType(ACCOUNT_TYPE);
		if(accounts.length > 1 || accounts == null) {
			return null;
		}
		else {
			return accManager.getPassword(accounts[0]);
		}
	}
	
	
	public void removeAccount() {
		
	}
}
