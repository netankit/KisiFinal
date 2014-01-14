package de.kisi.android;

import android.app.Application;
import android.util.Log;

public class KisiApplication extends Application {

	private static Application instance;
	private static boolean loggedIn;
	private static int place_holder;
	private static int lock_holder;
	private static boolean button_clicked;

	public static Application getApplicationInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		setLoggedIn(false);
		setPlace_holder(-1);
		setLock_holder(-2);
		setButton_clicked(false);

		// TODO: Uncomment this for release
		/*
		 * Thread.setDefaultUncaughtExceptionHandler(new
		 * UncaughtExceptionHandler(){
		 * 
		 * @Override public void uncaughtException(Thread arg0, Throwable arg1)
		 * { System.exit(1); } });
		 */
		de.kisi.android.db.DataManager.initialize(this);
		de.kisi.android.account.KisiAccountManager.initialize(this);
		de.kisi.android.api.KisiAPI.initialize(this);
		de.kisi.android.api.KisiLocationManager.initialize(this);
		// TODO: Uncomment this for release with geofance
		de.kisi.android.vicinity.manager.GeofenceManager.initialize(this);
		de.kisi.android.vicinity.LockInVicinityDisplayManager.initialize(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d("kisi", "KisiApplication.onTerminate");
		de.kisi.android.db.DataManager.getInstance().close();
	}

	public static boolean isLoggedIn() {
		return loggedIn;
	}

	public static void setLoggedIn(boolean loggedIn) {
		KisiApplication.loggedIn = loggedIn;
	}

	public static int getPlace_holder() {
		return place_holder;
	}

	public static void setPlace_holder(int place_holder) {
		KisiApplication.place_holder = place_holder;
	}

	public static int getLock_holder() {
		return lock_holder;
	}

	public static void setLock_holder(int lock_holder) {
		KisiApplication.lock_holder = lock_holder;
	}

	public static boolean isButton_clicked() {
		return button_clicked;
	}

	public static void setButton_clicked(boolean button_clicked) {
		KisiApplication.button_clicked = button_clicked;
	}
}
