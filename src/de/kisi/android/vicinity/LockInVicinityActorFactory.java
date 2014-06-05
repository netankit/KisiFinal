package de.kisi.android.vicinity;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.actor.*;

/**
 * This class is responsible for choosing the right actor. The main logic of
 * the program can be changed within this class.
 */
public class LockInVicinityActorFactory {

	private static DelayedExitActor delayedBluetoothActor = new DelayedExitActor(new StartPermanentBluetoothServiceActor());
	
	public static LockInVicinityActorInterface getActor(VicinityTypeEnum type){
		
		switch(type){
		case BluetoothLE: // BLE runs in suggest to unlock mode (this is decision of the server)
			return new ConfirmToUnlockActor();
		case BluetoothLEAutoUnlock: // BLE runs in automatic unlock mode (this is decision of the server)
			return new AutomaticUnlockActor();
		case Geofence: // Show that Geofence entered, and also activate BLE
			LockInVicinityActorInterface[] actors = new LockInVicinityActorInterface[2];
			actors[0] = new ConfirmToUnlockActor();
			actors[1] = delayedBluetoothActor;
			return new CompositActor(actors);
		case NFC: // NFC Tag with Lock information detected
			return new ConfirmToUnlockActor();
		default: // default state for future vicinity manager
			return new ConfirmToUnlockActor();
		}
	}
	
	public static LockInVicinityActorInterface getActor(Locator locator){
		if(locator==null)
			return new ConfirmToUnlockActor();
		
		if("NFC".equals(locator.getType())){
			if(locator.isAutoUnlockEnabled())
				return new NFCAutomaticUnlockActor();
			if(locator.isSuggestUnlockEnabled())
				return new HighlightLockActor();
		}
		return new ConfirmToUnlockActor();
	}
}
