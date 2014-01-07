package de.kisi.android.vicinity.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

public class NFCReceiver extends BroadcastReceiver{

	private NfcAdapter nfcAdapter;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		Log.i("NFC",intent.getAction());
	}

}
