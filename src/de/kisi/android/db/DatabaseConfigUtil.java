package de.kisi.android.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

// you have to run this after EVERY change in the kisi.model.*
//see http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html#Config-Optimization
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	@SuppressWarnings("unused")
	private static final Class<?>[] classLock = new Class[] {
	    Lock.class,
	  };
	@SuppressWarnings("unused")
	private static final Class<?>[] classPlace = new Class[] {
	    Place.class,
	  };
	public static void main(String[] args) throws Exception {
//	    writeConfigFile("ormlite_config.txt", classLock);
	    writeConfigFile("ormlite_config.txt");
	  }
}
