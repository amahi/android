package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class ResendFriendRequestResponse{

    @SerializedName("message")
	private String message;

    @SerializedName("success")
	private boolean success;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	@Override
 	public String toString(){
		return 
			"ResendFriendRequestResponse{" + 
			"“message” = '" + message + '\'' + 
			",“success” = '" + success + '\'' + 
			"}";
		}
}
