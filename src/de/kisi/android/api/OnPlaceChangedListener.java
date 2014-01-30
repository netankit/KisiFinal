package de.kisi.android.api;

import de.kisi.android.model.Place;
/**
 * This Listener is used to get regularly updates about
 * changing model data. This also contains Locks or Locators
 * contained in the places. 
 */
public interface OnPlaceChangedListener {

	public void onPlaceChanged(Place[] newPlaces);
}
