package de.kisi.android.vicinity.actor;

import android.content.Intent;
import de.kisi.android.KisiApplication;
import de.kisi.android.api.PlacesHandler;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.manager.BluetoothAutoUnlockService;

public class AutomaticUnlockActor implements LockInVicinityActorInterface {

	// directly send a unlock process to the server
	@Override
	public void actOnEntry(int placeID, int lockId) {
		Place place = PlacesHandler.getInstance().getPlaceById(placeID);
		for(Lock lock : place.getLocks())
			if(lock.getId()==lockId) {
				Intent intent = new Intent(KisiApplication.getInstance(), BluetoothAutoUnlockService.class);
				intent.putExtra("Lock", lock.getId());
				KisiApplication.getInstance().startService(intent);
			}
	}

	// we do not have to do anything
	@Override
	public void actOnExit(int placeID, int lockId) {
		
	}

	// directly send a unlock process to the server
	@Override
	public void actOnEntry(Locator locator) {
		Intent intent = new Intent(KisiApplication.getInstance(), BluetoothAutoUnlockService.class);
		intent.putExtra("Lock", locator.getLock().getId());
		KisiApplication.getInstance().startService(intent);
	}

	// wo do not have to do anything
	@Override
	public void actOnExit(Locator locator) {
		
	}

	
}
