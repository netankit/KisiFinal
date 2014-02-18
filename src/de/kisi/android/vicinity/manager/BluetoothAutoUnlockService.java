package de.kisi.android.vicinity.manager;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;
import de.kisi.android.notifications.AutoUnlockNotificationInfo;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


//this service is responsible to handle automatic unlock
//you need a seperated service for this task, because the BLE beacon detection is handled by an IntentService 
//for an IntentService it isn't possible offer an anonymous class 
//see https://stackoverflow.com/questions/17237746/sending-message-to-a-handler-on-a-dead-thread
public class BluetoothAutoUnlockService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int lockId = intent.getIntExtra("Lock", -1);
		
		if(lockId != -1 ) {
			Lock lock = KisiAPI.getInstance().getLockById(lockId);
			unlockLock(lock);
		}
				
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	private void  unlockLock(final Lock lock) {
		final AutoUnlockNotificationInfo info = de.kisi.android.notifications.NotificationManager.getBLEAutoUnlockNotifiction(lock);
		final Thread myThread = new Thread();
		KisiAPI.getInstance().unlock(lock, new  UnlockCallback(){
			@Override
			public void onUnlockSuccess(String message) {
				info.success = true;
				info.message = message;
				de.kisi.android.notifications.NotificationManager.getBLEAutoUnlockNotifictionResult(info);
			}

			@Override
			public void onUnlockFail(String message) {
				info.success = false;
				info.message = message;
				de.kisi.android.notifications.NotificationManager.getBLEAutoUnlockNotifictionResult(info);
				}}
		);

	}
	
}
