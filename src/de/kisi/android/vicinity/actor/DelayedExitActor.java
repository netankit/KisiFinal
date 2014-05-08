package de.kisi.android.vicinity.actor;

import android.util.Pair;
import de.kisi.android.model.Locator;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class DelayedExitActor implements LockInVicinityActorInterface{

	private LockInVicinityActorInterface actor;
	private DelayedExitThread thread;
	private class DelayedExitThread extends Thread{
		private static final int delay = 10000;// 10 seconds
		private boolean valid = true;
		private Pair<Integer,Integer> placeLock = null;
		private Locator locator = null;
		public DelayedExitThread(int placeId, int lockId){
			this.placeLock = new Pair<Integer,Integer>(placeId,lockId);
		}
		public DelayedExitThread(Locator locator){
			this.locator = locator;
		}
		public void run(){
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(this){
				if(valid){
					if(placeLock != null)
						actor.actOnExit(placeLock.first,placeLock.second);
					if(locator != null)
						actor.actOnExit(locator);
				}
				valid=false;
			}
			
		}
		public boolean invalidate(){
			boolean result;
			synchronized(this){
				result = valid;
				valid = false;
			}
			return result;
		}
	}
	
	public DelayedExitActor(LockInVicinityActorInterface actor){
		this.actor = actor;
	}
	@Override
	public void actOnEntry(int placeId, int lockId) {
		if(thread != null)
			thread.invalidate();
		actor.actOnEntry(placeId, lockId);
	}

	@Override
	public void actOnExit(int placeId, int lockId) {
		thread = new DelayedExitThread(placeId,lockId);
		thread.start();
	}

	@Override
	public void actOnEntry(Locator locator) {
		if(thread != null)
			thread.invalidate();
		actor.actOnEntry(locator);
	}

	@Override
	public void actOnExit(Locator locator) {
		thread = new DelayedExitThread(locator);
		thread.start();
	}

}
