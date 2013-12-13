package de.kisi.android.db;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

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
    private DatabaseManager dbManager;
 
    private DataManager(Context ctx)
    {
        try {
            dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            lockDao = db.getLockDao();
            placeDao = db.gePlaceDao();
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
    
 
    
    public void deleteDB() {
    	db.clear();
    }
    
    public void close() {
    	db.close();
    	dbManager.releaseHelper(db);
    }
    
}
