package de.kisi.android.vicinity.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

public class BluetoothLEService extends IntentService implements IBeaconConsumer{

	public BluetoothLEService(){
		super("BluetoothLEService");
	}

	private class RegionContainer{
		Hashtable<Integer,Region> hashtable = new Hashtable<Integer,Region>();
		Hashtable<Region,Integer> keys = new Hashtable<Region,Integer>();
		
		public Collection<Region> values(){
			LinkedList<Region> regions = new LinkedList<Region>();
			for(Region r : hashtable.values())
				regions.add(r);
			return regions;
		}
		public boolean containsKey(Integer key){
			return hashtable.containsKey(key);
		}
		public void put(Integer key, Region value){
			hashtable.put(key, value);
			keys.put(value, key);
		}
		public Region get(Integer key){
			return hashtable.get(key);
		}
		public void remove(Region region){
			Integer key = keys.get(region);
			keys.remove(region);
			hashtable.remove(key);
		}
	}
	private final IBinder mBinder = new LocalBinder();
	private RegionContainer regions = new RegionContainer();
	public class LocalBinder extends Binder {
		public BluetoothLEService getService(){
			return BluetoothLEService.this;
		}
    }		

	private IBeaconManager iBeaconManager;
	
	@Override
	public IBinder onBind(Intent arg0) {
		//iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		//iBeaconManager.bind(this);
		return mBinder;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.getExtras()!=null && intent.getExtras().getBoolean("foreground", false)){
			Log.i("BLE","start foreground");
			Pair<Integer,Notification> notification = NotificationManager.getBLEServiceNotification(this);
			if(notification != null){
				Log.i("BLE","notification id "+notification.first);
				Log.i("BLE","notification "+notification.second);
				startForeground(notification.first, notification.second);
			}
		}else{
			Log.i("BLE","start background");
			try{
				stopForeground(true);
				NotificationManager.notifyBLEServiceNotificationDeleted();
			}catch(Exception e){}
		}
		iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		iBeaconManager.bind(this);
	    return START_STICKY;
	}

	private static HashSet<Integer> unlockSuggest = new HashSet<Integer>();
	private static HashSet<Integer> automaticUnlock = new HashSet<Integer>();
			
	@Override
	public void onIBeaconServiceConnect() {
		Log.i("BLE","connect");
		iBeaconManager.setRangeNotifier(new RangeNotifier(){

			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				IBeacon beacon = iBeacons.iterator().next();
				String beaconId[] = region.getUniqueId().split(" ");
				int placeId = Integer.parseInt(beaconId[1]);
				int lockId = Integer.parseInt(beaconId[3]);
				int locatorId = Integer.parseInt(beaconId[5]);
				Locator locator;
				try{
					locator = KisiAPI.getInstance().getPlaceById(placeId).getLockById(lockId).getLocatorById(locatorId);
					if(locator.isSuggestUnlockEnabled()){
						LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLE);
						if(unlockSuggest.contains(locatorId) && locator.getSuggestUnlockTreshold()<beacon.getRssi()){
							unlockSuggest.remove(locatorId);
							actor.actOnExit(placeId,lockId);
						}
						if(!unlockSuggest.contains(locatorId) && locator.getSuggestUnlockTreshold()>beacon.getRssi()){
							unlockSuggest.add(locatorId);
							actor.actOnEntry(placeId,lockId);
						}
					}
					if(locator.isAutoUnlockEnabled()){
						LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLEAutoUnlock);
						if(automaticUnlock.contains(locatorId) && locator.getAutoUnlockTreshold()<beacon.getRssi()){
							automaticUnlock.remove(locatorId);
							actor.actOnExit(placeId,lockId);
						}
						if(!automaticUnlock.contains(locatorId) && locator.getAutoUnlockTreshold()>beacon.getRssi()){
							automaticUnlock.add(locatorId);
							actor.actOnEntry(placeId,lockId);
						}
					}
				}catch(NullPointerException e){
					
				}
				/*
				for(IBeacon b:iBeacons){
					Log.i("BLE","Rssi "+b.getRssi());
					Log.i("BLE","Acc "+b.getAccuracy());
				}
				*/
			}});
		iBeaconManager.setMonitorNotifier(new MonitorNotifier(){

			@Override
			public void didEnterRegion(Region region) {
				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLE);
				String beaconId[] = region.getUniqueId().split(" ");
				int placeId = Integer.parseInt(beaconId[1]);
				int lockId = Integer.parseInt(beaconId[3]);
				actor.actOnEntry(placeId,lockId);
				
			}

			@Override
			public void didExitRegion(Region region) {
				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLE);
				String beaconId[] = region.getUniqueId().split(" ");
				int placeId = Integer.parseInt(beaconId[1]);
				int lockId = Integer.parseInt(beaconId[3]);
				actor.actOnExit(placeId,lockId);
				
			}

			@Override
			public void didDetermineStateForRegion(int state,
					Region region) {
				// TODO Auto-generated method stub
				
			}
			
		});
		KisiAPI.getInstance().registerOnPlaceChangedListener(new OnPlaceChangedListener(){

			@Override
			public void onPlaceChanged(Place[] newPlaces) {
				registerPlaces(newPlaces);
				
			}
		});
		registerPlaces(KisiAPI.getInstance().getPlaces());
	}
	
	private void registerPlaces(Place[] places){
		Log.i("BLE","start register");
        try {
        	// Get Registered Regions
        	Collection<Region> curRegions = regions.values();
        	for(Place p :places){
        		for(Locator locator:p.getLocators()){
        			if(locator.getType()!=null && locator.getType().equals("BLE")){
        				if(regions.containsKey(locator.getId())){
        					// Remove Region from Delete list
        					curRegions.remove(regions.get(locator.getId()));
        				}else{
        					int major = locator.getMajor();
        					int minor = locator.getMinor();
        					Region region = new Region("Place: "+locator.getPlaceId()+" Lock: "+locator.getLockId()+" Locator: "+locator.getId(), "DE9D14A1-1C16-4114-9B68-3B2435C6B99A", major,minor);
        					Log.i("BLE","Set iBeacon "+region.getUniqueId());
        					if(!locator.isSuggestUnlockEnabled())
        						iBeaconManager.startMonitoringBeaconsInRegion(region);
        					if(locator.isAutoUnlockEnabled() || locator.isSuggestUnlockEnabled())
        						iBeaconManager.startRangingBeaconsInRegion(region);
        					regions.put(locator.getId(), region);
        				}
        			}
        		}
        	}
        	// Delete Regions that not in the list anymore
        	for(Region region : curRegions){
        		iBeaconManager.stopMonitoringBeaconsInRegion(region);
				iBeaconManager.stopRangingBeaconsInRegion(region);
				Log.i("BLE","remove "+region.getUniqueId());
        		regions.remove(region);
        	}
        } catch (RemoteException e) {   }			
	}

}
