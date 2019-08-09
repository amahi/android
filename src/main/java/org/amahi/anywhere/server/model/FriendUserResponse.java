package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendUserResponse{

    @SerializedName("data")
	private List<FriendUserItem> friendUsers;

    @SerializedName("success")
	private boolean success;

	public void setFriendUsers(List<FriendUserItem> friendUsers){
		this.friendUsers = friendUsers;
	}

	public List<FriendUserItem> getFriendUsers(){
		return friendUsers;
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
			"FriendUserResponse{" +
			"friend_user = '" + friendUsers + '\'' +
			",success = '" + success + '\'' +
			"}";
		}
}
