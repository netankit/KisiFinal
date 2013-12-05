package de.kisi.android.model;



 
public class Lock {
	private int id;
	private String name;
	private int place_id;
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
		return place_id;
	}

	public int getPlace_id() {
		return place_id;
	}

	public void setPlace_id(int place_id) {
		this.place_id = place_id;
	}


	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
