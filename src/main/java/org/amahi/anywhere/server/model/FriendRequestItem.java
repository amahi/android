package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class FriendRequestItem{

    @SerializedName("amahi_user_id")
	private int amahiUserId;

    @SerializedName("pin")
	private String pin;

    @SerializedName("last_requested_at")
	private String lastRequestedAt;

    @SerializedName("updated_at")
	private String updatedAt;

    @SerializedName("system_id")
	private int systemId;

    @SerializedName("created_at")
	private String createdAt;

    @SerializedName("id")
	private int id;

    @SerializedName("invite_token")
	private String inviteToken;

    @SerializedName("email")
	private String email;

    @SerializedName("status_txt")
	private String statusTxt;
	private int status;

	public void setAmahiUserId(int amahiUserId){
		this.amahiUserId = amahiUserId;
	}

	public int getAmahiUserId(){
		return amahiUserId;
	}

	public void setPin(String pin){
		this.pin = pin;
	}

	public String getPin(){
		return pin;
	}

	public void setLastRequestedAt(String lastRequestedAt){
		this.lastRequestedAt = lastRequestedAt;
	}

	public String getLastRequestedAt(){
		return lastRequestedAt;
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

	public void setInviteToken(String inviteToken){
		this.inviteToken = inviteToken;
	}

	public String getInviteToken(){
		return inviteToken;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public void setStatusTxt(String statusTxt){
		this.statusTxt = statusTxt;
	}

	public String getStatusTxt(){
		return statusTxt;
	}

	public void setStatus(int status){
		this.status = status;
	}

	public int getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"FriendRequestItem{" + 
			"amahi_user_id = '" + amahiUserId + '\'' + 
			",pin = '" + pin + '\'' + 
			",last_requested_at = '" + lastRequestedAt + '\'' + 
			",updated_at = '" + updatedAt + '\'' + 
			",system_id = '" + systemId + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",id = '" + id + '\'' + 
			",invite_token = '" + inviteToken + '\'' + 
			",email = '" + email + '\'' + 
			",status_txt = '" + statusTxt + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}
