package de.kisi.android.vicinity.actor;

import android.content.Intent;
import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class NFCAutomaticUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry(Locator locator) {
		Intent intent = new Intent(KisiApplication.getInstance(), KisiMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Type", "unlock");
		intent.putExtra("Sender", "NFC");
		intent.putExtra("Place", locator.getPlaceId());
		intent.putExtra("Lock", locator.getLockId());
		KisiApplication.getInstance().startActivity(intent);
	}

	@Override
	public void actOnExit(Locator locator) {
	}

}
