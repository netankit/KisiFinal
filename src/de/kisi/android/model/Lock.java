package de.kisi.android.model;

import org.json.JSONException;
import org.json.JSONObject;
 
public class Lock {
	private int id;
	private String name;
	private int place_id;
	private String updated_at;
	private String last_accessed_at;
	
	public Lock(JSONObject json) {
		try {
			id = json.getInt("id");
			name = json.getString("name");
			place_id = json.getInt("place_id");
			updated_at = json.getString("updated_at");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getPlaceId() {
		return place_id;
	}

	public String getUpdatedAt() {
		return updated_at;
	}

}
