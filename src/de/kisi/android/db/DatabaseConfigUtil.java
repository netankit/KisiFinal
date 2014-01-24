package de.kisi.android.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;


// you have to run this after EVERY change in the kisi.model.*
//see http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html#Config-Optimization
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	public static void main(String[] args) throws Exception {
		//OrmLiteConfigUtil.writeConfigFile("ormlite_config.txt");
		writeConfigFile("ormlite_config.txt", new Class< ? >[] {User.class, Lock.class, Place.class});
	  }
}
