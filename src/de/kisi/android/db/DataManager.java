package de.kisi.android.db;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class DataManager {

	private static DataManager instance;
	
	public static DataManager getInstance() {
		return instance;
	}
	
	public static void initialize(Context context){
		instance =  new DataManager(context);
	}
	
	
	private DatabaseHelper db;
    private Dao<Lock, Integer> lockDao;
    private Dao<Place, Integer> placeDao;
    private Dao<Locator, Integer> locatorDao;
    private DatabaseManager dbManager;
 
    private DataManager(Context context)
    {
        try {
            dbManager = new DatabaseManager();
            db = dbManager.getHelper(context);
            lockDao = db.getLockDao();
            placeDao = db.getPlaceDao();
            locatorDao = db.getLocatorDao();
        }catch (SQLException e) {
            e.printStackTrace();
        }


    }
    
    public void saveLocks(final Lock[] locks) {    	 
    	try {
			lockDao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					for (Lock l : locks) {
						lockDao.createOrUpdate(l);
					}
					return null;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void savePlaces(final Place[] places) {
    	try {
			//put the hole operation into one transaction
			placeDao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {

					for (Place p : places) {
						placeDao.createOrUpdate(p);
					}
					return null;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    public void saveLocators(final Locator[] locators) {
    	try {
			//put the hole operation into one transaction
			locatorDao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {

					for (Locator l : locators) {
						locatorDao.createOrUpdate(l);
					}
					return null;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public List<Place> getAllPlaces() {
    	List<Place> result = null;
    	try {
    		result = placeDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return result;
    }
    

    public List<Lock> getAllLocks() {
    	List<Lock> result = null;
    	try {
    		result =  lockDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return result;
    }
    
    
    public List<Locator> getAllLocators() {
    	List<Locator> result = null;
    	try {
    		result =  locatorDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return result;
    }
    
 
    
    public void deleteDB() {
    	db.clear();
    }
    
    public void close() {
    	db.close();
    	dbManager.releaseHelper(db);
    }
    
}
