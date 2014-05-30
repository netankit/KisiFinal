package de.kisi.android.api.calls;

import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

public class UpdateLocksCall {
	private Place place;
	private OnPlaceChangedListener listener;
	public UpdateLocksCall(final Place place, final OnPlaceChangedListener listener) {
		this.place = place;
		this.listener = listener;
	}

}
