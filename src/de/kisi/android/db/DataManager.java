package de.kisi.android.db;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;

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
    private Dao<User, Integer> userDao;
    private DatabaseManager dbManager;
 
    private DataManager(Context context)
    {
        try {
            dbManager = new DatabaseManager();
            db = dbManager.getHelper(context);
            lockDao = db.getLockDao();
            placeDao = db.getPlaceDao();
            userDao = db.getUserDao();
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

    public void saveUser(User user) {
    	try {
			userDao.createOrUpdate(user);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
    
    
    public User getUser() {
    	List<User> result = null;
    	try {
    		result =  userDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	if(result == null||result.size() == 0) {
    		return null;
    	}
    	else{
    		return result.get(0);
    	}
    }
 
    
    public void deleteDB() {
    	db.clear();
    }
    
    
    public void deletePlaceLockFromDB() {
    	db.clearPlaceLock();
    }
    
    public void close() {
    	db.close();
    	dbManager.releaseHelper(db);
    }
    
}
