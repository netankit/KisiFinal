package de.kisi.android.model;

import android.widget.Toast;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;

public class User {
	@DatabaseField(id = true, index=true)
	private int id;
	@DatabaseField
	private String authentication_token;
	@DatabaseField
	private String email;
	@DatabaseField
	@SerializedName("ei_plan_id")
	private String eiPlanId;
	@DatabaseField
	@SerializedName("first_name")
	private String firstName;
	@DatabaseField
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
	
	public boolean isDemo() {
		String email = getEmail();
		return KisiApplication.getInstance().getString(R.string.demo_email).equals(email);
	}

}
