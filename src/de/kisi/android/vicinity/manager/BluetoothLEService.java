package de.kisi.android.vicinity.manager;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BluetoothLEService extends Service implements IBeaconConsumer{

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
		iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		iBeaconManager.bind(this);
		return mBinder;
	}


	@Override
	public void onIBeaconServiceConnect() {
		Log.i("BLE","connect");
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
		Log.i("BLE","Places.lenght = "+places.length);
        try {
        	Collection<Region> curRegions = regions.values();
        	for(Place p :places){
        		for(Lock l:p.getLocks()){
        			if(regions.containsKey(l.getId())){
        				curRegions.remove(regions.get(l.getId()));
        			}else{
        				Region region = new Region("Place: "+p.getId()+" Lock: "+l.getId(), "DE9D14A1-1C16-4114-9B68-3B2435C6B99A", 65535,65535);
        				iBeaconManager.startMonitoringBeaconsInRegion(region);
        				regions.put(l.getId(), region);
        			}
        		}
        	}
        	for(Region r : curRegions){
        		iBeaconManager.stopMonitoringBeaconsInRegion(r);
        		regions.remove(r);
        	}
        } catch (RemoteException e) {   }			
	}

}
