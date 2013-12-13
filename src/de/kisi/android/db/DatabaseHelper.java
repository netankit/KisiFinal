package de.kisi.android.db;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "Kisi.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the Lock table;

	private Dao<Lock, Integer> lockDao = null;
    private RuntimeExceptionDao<Lock, Integer> lockRuntimeDao = null;
	
    private Dao<Place, Integer> placeDao = null;
    private RuntimeExceptionDao<Place, Integer> placeRuntimeDao = null;

    
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, de.kisi.android.R.raw.ormlite_config );
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Place.class);
			TableUtils.createTable(connectionSource, Lock.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

//		// here we try inserting data in the on-create as a test
//		RuntimeExceptionDao<SimpleData, Integer> dao = getSimpleDataDao();
//		long millis = System.currentTimeMillis();
//		// create some entries in the onCreate
//		SimpleData simple = new SimpleData(millis);
//		dao.create(simple);
//		simple = new SimpleData(millis + 1);
//		dao.create(simple);
//		Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: " + millis);
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
//		try {
//			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
//			TableUtils.dropTable(connectionSource, SimpleData.class, true);
//			// after we drop the old databases, we create the new ones
//			onCreate(db, connectionSource);
//		} catch (SQLException e) {
//			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
//			throw new RuntimeException(e);
//		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our Lock class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Lock, Integer> getLockDao() throws SQLException {
		if (lockDao == null) {
			lockDao = getDao(Lock.class);
		}
		return lockDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Lock class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<Lock, Integer> getLockDataDao() {
		if (lockRuntimeDao == null) {
			lockRuntimeDao = getRuntimeExceptionDao(Lock.class);
		}
		return lockRuntimeDao;
	}
	
	/**
	 * Returns the Database Access Object (DAO) for our Place class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Place, Integer> gePlaceDao() throws SQLException {
		if (placeDao == null) {
			placeDao = getDao(Place.class);
		}
		return placeDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Place class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<Place, Integer> getPlaceDataDao() {
		if (placeRuntimeDao == null) {
			placeRuntimeDao = getRuntimeExceptionDao(Place.class);
		}
		return placeRuntimeDao;
	}


	public void clear() {
		try {
			TableUtils.clearTable(connectionSource, Place.class);
			TableUtils.clearTable(connectionSource, Lock.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		lockRuntimeDao = null;
		placeRuntimeDao = null;
	}
}
