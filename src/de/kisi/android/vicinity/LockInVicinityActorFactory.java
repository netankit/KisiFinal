package de.kisi.android.vicinity;

import de.kisi.android.vicinity.actor.*;

public class LockInVicinityActorFactory {

	public static LockInVicinityActorInterface getActor(VicinityTypeEnum type){
		return new ConfirmToUnlockActor();
	}
}
