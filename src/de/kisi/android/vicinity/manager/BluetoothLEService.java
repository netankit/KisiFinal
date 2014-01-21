package de.kisi.android.vicinity.manager;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.app.Service;
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
		//iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		//iBeaconManager.bind(this);
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.getExtras()!=null && intent.getExtras().getBoolean("foreground", false)){
			NotificationCompat.Builder nc = new NotificationCompat.Builder(this);
			nc.setSmallIcon(R.drawable.ic_launcher);
			nc.setContentText("Bluetooth running");
			nc.setContentTitle("KISI");
			startForeground(1, nc.build());
		//}else{
		//	stopForeground(false);
		}
		iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		iBeaconManager.bind(this);
	    return START_STICKY;
	}

	@Override
	public void onIBeaconServiceConnect() {
		Log.i("BLE","connect");
		iBeaconManager.setRangeNotifier(new RangeNotifier(){

			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				for(IBeacon b:iBeacons){
					Log.i("BLE","Rssi "+b.getRssi());
					Log.i("BLE","Acc "+b.getAccuracy());
				}
				
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
        					iBeaconManager.startMonitoringBeaconsInRegion(region);
        					iBeaconManager.startRangingBeaconsInRegion(region);
        					regions.put(locator.getId(), region);
        				}
        			}
        		}
        	}
        	for(Region region : curRegions){
        		iBeaconManager.stopMonitoringBeaconsInRegion(region);
				iBeaconManager.startRangingBeaconsInRegion(region);
				Log.i("BLE","remove "+region.getUniqueId());
        		regions.remove(region);
        	}
        } catch (RemoteException e) {   }			
	}

}
