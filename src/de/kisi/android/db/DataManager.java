package de.kisi.android.db;

import java.sql.SQLException;
import java.util.List;

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
            // TODO: Exception Handling
            e.printStackTrace();
        }
 
    }
    
    public void saveLocks(Lock[] locks) {
    	for(Lock l: locks) {
    		try {
				locksDao.createOrUpdate(l);
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    public void savePlaces(Place[] places) {
    	for(Place p:places) {
    		try {
				placeDao.createOrUpdate(p);
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public List<Place> getAllPlaces() {
    	try {
			return placeDao.queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    
    public Place[] getAllPlacesArray() {
    	List<Place> pl = null;
    	try {
    		pl = placeDao.queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Place[] pa = new Place[pl.size()];
    	for(int i = 0; i < pl.size(); i++) {
    		pa[i] = pl.get(i);
    	}
    	return pa;
    }
    
    
    public List<Lock> getAllLocks() {
    	try {
			return locksDao.queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
 
    
    //TODO: implement 
    public void deleteDB() {
    	
    }
    
}
