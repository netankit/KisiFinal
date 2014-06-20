package de.kisi.android.api;

import java.util.LinkedList;
import java.util.List;

public class OnPlaceChangedEventHandler {
	// -------------------- Singleton Stuff: --------------------
	private static OnPlaceChangedEventHandler instance;
	public static OnPlaceChangedEventHandler getInstance(){
		if(instance == null)
			instance = new OnPlaceChangedEventHandler();
		return instance;
	}
	private OnPlaceChangedEventHandler(){
	}
	
	// -------------------- On Place Changed handling: --------------------
	private List<OnPlaceChangedListener> registeredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> unregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> newregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	
	/**
	 * Notifies all the registered OnPlaceChangedListener that some Data regarding
	 * the Places has changed.
	 */
	public void notifyAllOnPlaceChangedListener(){
		// This have to be done this way, lists are not allowed to be modified 
		// during a foreach loop
		for(OnPlaceChangedListener listener : unregisteredOnPlaceChangedListener) {
			registeredOnPlaceChangedListener.remove(listener);
		}
		registeredOnPlaceChangedListener.addAll(newregisteredOnPlaceChangedListener);
		newregisteredOnPlaceChangedListener.clear();
		for(OnPlaceChangedListener listener : registeredOnPlaceChangedListener) {
			listener.onPlaceChanged(KisiAPI.getInstance().getPlaces());
		}
	}
	
	/**
	 * Register for interest in any changes in the Places.
	 * The listener is fired when a Place was added or deleted
	 * or any change occur within the Place like a Lock or Locator has been added. 
	 * 
	 * Sometimes on a total refresh there can be a lot of PlaceChanges in a short
	 * period of time, so make sure that the client can handle this.
	 * @param listener
	 */
	public void registerOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			newregisteredOnPlaceChangedListener.add(listener);
	}
	
	/**
	 * Unregister for the listener registered in registerOnPlaceChangedListener()
	 * @param listener
	 */
	public void unregisterOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			unregisteredOnPlaceChangedListener.add(listener);
	}
}
