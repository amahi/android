package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendRequestResponse{

    @SerializedName("success")
	private boolean success;

    @SerializedName("data")
	private List<FriendRequestItem> friendRequests;

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setFriendRequests(List<FriendRequestItem> friendRequests){
		this.friendRequests = friendRequests;
	}

	public List<FriendRequestItem> getFriendRequests(){
		return friendRequests;
	}

	@Override
 	public String toString(){
		return 
			"FriendRequestResponse{" + 
			"success = '" + success + '\'' + 
			",friend_request = '" + friendRequests + '\'' +
			"}";
		}
}
