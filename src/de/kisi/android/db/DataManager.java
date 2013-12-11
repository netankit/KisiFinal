package de.kisi.android.db;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;
import android.util.Log;

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
    private Dao<Lock, Integer> locksDao;
    private Dao<Place, Integer> placeDao;
 
    private DataManager(Context ctx)
    {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            locksDao = db.getLockDao();
            placeDao = db.gePlaceDao();
        }catch (SQLException e) {
            e.printStackTrace();
        }


    }
    
    public void saveLocks(final Lock[] locks) {    	 
		final long time = System.currentTimeMillis();
		try {
			locksDao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					for (Lock l : locks) {
						locksDao.createOrUpdate(l);
					}
					Log.e("saveLocks",
							String.valueOf(System.currentTimeMillis() - time)
									+ " ms");
					return null;

				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void savePlaces(final Place[] places) {
    	final long time= System.currentTimeMillis();

		try {
			//put the hole operation into one transaction
			placeDao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {

					for (Place p : places) {
						placeDao.createOrUpdate(p);
					}
					Log.e("savePlaces", String.valueOf(System.currentTimeMillis()-time) + " ms");
					return null;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	
    	
    }
    
    public List<Place> getAllPlaces() {
    	try {
			return placeDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    
    public Place[] getAllPlacesArray() {
    	List<Place> pl = null;
    	long time= System.currentTimeMillis();
    	try {
    		pl = placeDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	Log.e("getAllPlacesArray queryForAll()", String.valueOf(System.currentTimeMillis()-time) + " ms");
    	return pl.toArray(new Place[0]);
    }
    
    
    public List<Lock> getAllLocks() {
    	try {
			return locksDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
 
    
    //TODO: implement 
    public void deleteDB() {
    	
    	DatabaseManager dbManager = new DatabaseManager();
    	dbManager.releaseHelper(db);
    }
    
}
