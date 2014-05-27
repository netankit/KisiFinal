package de.kisi.android.vicinity;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.actor.*;

/**
 * This class is responsible for choosing the right actor. The main logic of
 * the program can be changed within this class.
 */
public class LockInVicinityActorFactory {

	private static DelayedExitActor delayedBluetoothActor = new DelayedExitActor(new StartPermanentBluetoothServiceActor());
	
	
	public static LockInVicinityActorInterface getGeofenceActor(){
		LockInVicinityActorInterface[] actors = new LockInVicinityActorInterface[2];
		actors[0] = new ConfirmToUnlockActor();
		actors[1] = delayedBluetoothActor;
		return new CompositActor(actors);
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
		if("BLE".equals(locator.getType())){
			if(locator.isAutoUnlockEnabled())
				return new AutomaticUnlockActor();
			if(locator.isSuggestUnlockEnabled())
				return new ConfirmToUnlockActor();
		}
		
		return new ConfirmToUnlockActor();
	}
}
