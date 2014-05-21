package de.kisi.android.vicinity.manager;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

public class NFCReceiver extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try{
	        // Read the first record which contains the NFC data
			Intent intent = getIntent();
	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        NdefRecord relayRecord = ((NdefMessage)rawMsgs[0]).getRecords()[0];

	        // Get the Data on the Tag
	        String nfcData = new String(relayRecord.getPayload());
	        
	        // Display the data on the tag for debuging
	        //Toast.makeText(this, nfcData, Toast.LENGTH_SHORT).show();
	        
	        
	        // Test all locators for equality to the data string
	        for(Place place : KisiAPI.getInstance().getPlaces()){
	        	for(Lock lock : place.getLocks()){
	        		for(Locator locator : lock.getLocators()){
	        			if (nfcData.equals(locator.getTag())){
	        		        // get actor for NFC
	        				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(locator);
	        				// act
	        				actor.actOnEntry(locator);
	        			}
	        		}
	        	}
	        }
	 
	    }catch(Exception e){
	    }finally{
	        // Just finish the activity
	        finish();
	    }
	}

	
}