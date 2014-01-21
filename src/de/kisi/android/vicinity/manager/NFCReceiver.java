package de.kisi.android.vicinity.manager;

import java.io.IOException;
import java.nio.charset.Charset;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

public class NFCReceiver extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Write Data on the Tag
		//NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		//Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		//NFCReceiver.writeTag(this, tag, "{535227EB-4329-4540-8DC7-FCE0292D8769}");
		
		
		try{
	        // Read the first record which contains the NFC data
			Intent intent = getIntent();
	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        NdefRecord relayRecord = ((NdefMessage)rawMsgs[0]).getRecords()[0];
	        String nfcData = new String(relayRecord.getPayload());
	 
	        // Display the data on the tag
	        Toast.makeText(this, nfcData, Toast.LENGTH_SHORT).show();
			LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.NFC);
	        
	        // Act 
	        for(Place place : KisiAPI.getInstance().getPlaces()){
	        	for(Lock lock : place.getLocks()){
	        		for(Locator locator : lock.getLocators()){
	        			if (nfcData.equals(locator.getUdid())){
	        				actor.actOnEntry(place.getId(),lock.getId());
	        			}
	        		}
	        	}
	        }
	 
	        // Just finish the activity
	        finish();
	    }catch(Exception e){
	    }
	}

	
	// This method can be deleted when we have a seperate App to write NFC Tags
	public static boolean writeTag(Context context, Tag tag, String data) {     
	    // Record to launch Play Store if app is not installed
	    NdefRecord appRecord = NdefRecord.createApplicationRecord(context.getPackageName());
	 
	    // Record with actual data we care about
	    NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	                                            new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),
	                                            null, data.getBytes());
	 
	    // Complete NDEF message with both records
	    NdefMessage message = new NdefMessage(new NdefRecord[] {relayRecord,appRecord});
	 
	    try {
	        // If the tag is already formatted, just write the message to it
	        Ndef ndef = Ndef.get(tag);
	        if(ndef != null) {
	            ndef.connect();
	 
	            // Make sure the tag is writable
	            if(!ndef.isWritable()) {
	                return false;
	            }
	 
	            // Check if there's enough space on the tag for the message
	            int size = message.toByteArray().length;
	            if(ndef.getMaxSize() < size) {
	                return false;
	            }
	 
	            try {
	                // Write the data to the tag
	                ndef.writeNdefMessage(message);
	 
	                return true;
	            } catch (TagLostException tle) {
	                return false;
	            } catch (IOException ioe) {
	                return false;
	            } catch (FormatException fe) {
	                return false;
	            }
	        // If the tag is not formatted, format it with the message
	        } else {
	            NdefFormatable format = NdefFormatable.get(tag);
	            if(format != null) {
	                try {
	                    format.connect();
	                    format.format(message);
	 
	                    return true;
	                } catch (TagLostException tle) {
	                    return false;
	                } catch (IOException ioe) {
	                    return false;
	                } catch (FormatException fe) {
	                    return false;
	                }
	            } else {
	                return false;
	            }
	        }
	    } catch(Exception e) {
	    }
	 
	    return false;
	}
}