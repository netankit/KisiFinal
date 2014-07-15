package de.kisi.android.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Lock {
	@DatabaseField(id = true)
	private int id;
	@DatabaseField
	private String name;
	@DatabaseField
	@SerializedName("action_message")
	private String actionMessage;
	@DatabaseField
	@SerializedName("before_action_message")
	private String beforeActionMessage;
	@DatabaseField
	@SerializedName("during_action_message")
	private String duringActionMessage;
	@DatabaseField
	@SerializedName("after_action_message")
	private String afterActionMessage;
	@DatabaseField
	@SerializedName("place_id")
	private int placeId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=1)
	private Place place;

	@ForeignCollectionField(eager=false)
    private ForeignCollection<Locator> locators;	
	
	
	//	@SerializedName("updated_at")
//	private Date updatedAt;
//	@SerializedName("last_accessed_at")
//	private Date lastAccessedAt;
	
	public Lock() {};
	
	//TODO: clean this up later
	public List<Locator> getLocators() {
		Locator[] locatorArray = locators.toArray(new Locator[0]);
		List<Locator> result = new ArrayList<Locator>();
		for(Locator l: locatorArray) {
			result.add(l);
		}
		return result;
	}
	
	public Locator getLocatorById(int locatorId) {
		for(Locator l:locators)
			if(l.getId() == locatorId)
				return l;
		return null;
	}
	
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

	public String getActionMessage() {
		return actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

	public String getBeforeActionMessage() {
		return beforeActionMessage;
	}

	public void setBeforeActionMessage(String beforeActionMessage) {
		this.beforeActionMessage = beforeActionMessage;
	}

	public String getDuringActionMessage() {
		return duringActionMessage;
	}

	public void setDuringActionMessage(String duringActionMessage) {
		this.duringActionMessage = duringActionMessage;
	}

	public String getAfterActionMessage() {
		return afterActionMessage;
	}

	public void setAfterActionMessage(String afterActionMessage) {
		this.afterActionMessage = afterActionMessage;
	}
}