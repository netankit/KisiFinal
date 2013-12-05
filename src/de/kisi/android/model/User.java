package de.kisi.android.model;

import com.google.gson.annotations.SerializedName;

public class User {
	private int id;
	private String authentication_token;
	private String email;
	@SerializedName("ei_plan_id")
	private String eiPlanId;
	@SerializedName("first_name")
	private String firstName;
	@SerializedName("last_name")
	private String lastName;
	
//	private String updated_at;
//	private String created_at;
//	private String provider;
//	private boolean support_switching_allowed;
//	private int uid;
//	private String roles_mask;
	
	public int getId() {
		return id;
	}
	public String getEiPlanId() {
		return eiPlanId;
	}
	public void setEiPlanId(String eiPlanId) {
		this.eiPlanId = eiPlanId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAuthentication_token() {
		return authentication_token;
	}
	public void setAuthentication_token(String authentication_token) {
		this.authentication_token = authentication_token;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}
