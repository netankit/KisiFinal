package de.kisi.android.vicinity.actor;

import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

public class StartPermanentBluetoothServiceActor implements LockInVicinityActorInterface{

	@Override
	public void actOnEntry(int placeID, int lockId) {
		BluetoothLEManager.getInstance().startService(true);
		
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		BluetoothLEManager.getInstance().startService(false);
		
	}

	@Override
	public void actOnEntry(Locator locator) {
		BluetoothLEManager.getInstance().startService(true);
		
	}

	@Override
	public void actOnExit(Locator locator) {
		BluetoothLEManager.getInstance().startService(false);
		
	}

}
