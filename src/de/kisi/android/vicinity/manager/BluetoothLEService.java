package de.kisi.android.vicinity.manager;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.VicinityTypeEnum;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

public class BluetoothLEService extends Service implements IBeaconConsumer{

	private final IBinder mBinder = new LocalBinder();
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
		iBeaconManager.setMonitorNotifier(new MonitorNotifier(){

			@Override
			public void didEnterRegion(Region region) {
				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLE);
				String beaconId[] = region.getUniqueId().split(": ");
				int placeId = Integer.parseInt(beaconId[1]);
				actor.actOnEntry(placeId);
				
			}

			@Override
			public void didExitRegion(Region region) {
				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(VicinityTypeEnum.BluetoothLE);
				String beaconId[] = region.getUniqueId().split(": ");
				int placeId = Integer.parseInt(beaconId[1]);
				actor.actOnExit(placeId);
				
			}

			@Override
			public void didDetermineStateForRegion(int state,
					Region region) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		Place[] places = KisiAPI.getInstance().getPlaces();
        try {
        	for(Place p :places)
        		iBeaconManager.startMonitoringBeaconsInRegion(new Region("Place: "+p.getId(), "DE9D14A1-1C16-4114-9B68-3B2435C6B99A", 65535,65535));
        	
        } catch (RemoteException e) {   }			
	}

}
