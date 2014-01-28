package de.kisi.android.vicinity;

import de.kisi.android.vicinity.actor.*;

public class LockInVicinityActorFactory {

	public static LockInVicinityActorInterface getActor(VicinityTypeEnum type){
		switch(type){
		case BluetoothLE:
			return new ConfirmToUnlockActor();
		case Geofence:
			LockInVicinityActorInterface[] actors = new LockInVicinityActorInterface[2];
			actors[0] = new ConfirmToUnlockActor();
			actors[1] = new StartPermanentBluetoothServiceActor();
			return new CompositActor(actors);
		case NFC:
			return new AutomaticUnlockActor();
		case BluetoothLEAutoUnlock:
			return new AutomaticUnlockActor();
		default:
			return new ConfirmToUnlockActor();
		}
	}
}
