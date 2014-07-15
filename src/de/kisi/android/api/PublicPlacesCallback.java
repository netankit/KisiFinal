package de.kisi.android.api;

import de.kisi.android.model.Place;

/**
 * Callback for the public places call.
 *
 */
public interface PublicPlacesCallback {
	public void onResult(Place[] places);
}
