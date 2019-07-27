package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class FriendUserItem{

    @SerializedName("amahi_user_id")
	private int amahiUserId;

    @SerializedName("updated_at")
	private String updatedAt;

    @SerializedName("system_id")
	private int systemId;

    @SerializedName("created_at")
	private String createdAt;

    @SerializedName("id")
	private int id;

    @SerializedName("email")
	private String email;

	public void setAmahiUserId(int amahiUserId){
		this.amahiUserId = amahiUserId;
	}

	public int getAmahiUserId(){
		return amahiUserId;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public void setSystemId(int systemId){
		this.systemId = systemId;
	}

	public int getSystemId(){
		return systemId;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	@Override
 	public String toString(){
		return 
			"FriendUserItem{" + 
			"amahi_user_id = '" + amahiUserId + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",system_id = '" + systemId + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",id = '" + id + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}
