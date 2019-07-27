package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class NewFriendRequest{

    @SerializedName("id")
	private String id;

    @SerializedName("status_txt")
	private String statusTxt;

    @SerializedName("email")
	private String email;

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setStatusTxt(String statusTxt){
		this.statusTxt = statusTxt;
	}

	public String getStatusTxt(){
		return statusTxt;
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
			"NewFriendRequest{" + 
			"id = '" + id + '\'' + 
			",status_txt = '" + statusTxt + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}
