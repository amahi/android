package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class NewFriendRequestResponse{

    @SerializedName("success")
	private boolean success;

    @SerializedName("message")
	private String message;

    @SerializedName("request")
	private NewFriendRequest newFriendRequest;

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setNewFriendRequest(NewFriendRequest newFriendRequest){
		this.newFriendRequest = newFriendRequest;
	}

	public NewFriendRequest getNewFriendRequest(){
		return newFriendRequest;
	}

	@Override
 	public String toString(){
		return 
			"NewFriendRequestResponse{" + 
			"success = '" + success + '\'' + 
			",message = '" + message + '\'' + 
			",“new_friend_request” = '" + newFriendRequest + '\'' + 
			"}";
		}
}
