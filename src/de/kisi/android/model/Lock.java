package de.kisi.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;



@DatabaseTable(tableName = "Locks") 
public class Lock {
	@DatabaseField(id = true)
	private int id;
	@DatabaseField
	private String name;
	@DatabaseField
	@SerializedName("place_id")
	private int placeId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true)
	private Place place;
//	@SerializedName("updated_at")
//	private Date updatedAt;
//	@SerializedName("last_accessed_at")
//	private Date lastAccessedAt;
	
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getPlaceId() {
		return placeId;
	}


	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}


	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	public Place getPlace() {
		return place;
	}
	public void setPlace(Place place) {
		this.place = place;
	}

}
