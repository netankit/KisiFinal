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
	
	public boolean addAccount(String email, String password) {
		Account account = new Account(email, ACCOUNT_TYPE);
		return accManager.addAccountExplicitly(account, password, null);
	}
	
	public String getUserName(){
		Account[] accounts = accManager.getAccountsByType(ACCOUNT_TYPE);
		if(accounts == null || accounts.length > 1) {
			return null;
		}
		else {
			return accounts[0].name;
		}
	}
	
	
	public String getPassword() {
		Account[] accounts = accManager.getAccountsByType(ACCOUNT_TYPE);
		if(accounts.length == 0|| accounts.length > 1) {
			return null;
		}
		else {
			return accManager.getPassword(accounts[0]);
		}
	}
	
	
	public void removeAccount() {
		Account[] accounts = accManager.getAccountsByType(ACCOUNT_TYPE);
		if(accounts.length == 0|| accounts.length > 1) {
			return;
		}
		else {
			accManager.removeAccount(accounts[0], null, null);
		}
	}
}
