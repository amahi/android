package org.amahi.anywhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.amahi.anywhere.R;

import java.util.List;

public class FriendRequestsListAdapter extends RecyclerView.Adapter<FriendRequestsListAdapter.FriendRequestsListViewHolder> {
    private List<String> friendRequestsEmailList;
    private Context context;

    public FriendRequestsListAdapter(Context context, List<String> friendRequestsEmailList) {
        this.context = context;
        this.friendRequestsEmailList = friendRequestsEmailList;

    }

    @NonNull
    @Override
    public FriendRequestsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestsListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_requests_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsListViewHolder holder, int position) {
        holder.friendEmailText.setText(friendRequestsEmailList.get(position));

    }

    @Override
    public int getItemCount() {
        return friendRequestsEmailList.size();
    }

    class FriendRequestsListViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailText;

        FriendRequestsListViewHolder(View itemView) {
            super(itemView);
            friendEmailText = itemView.findViewById(R.id.text_friend_request_email);

        }
    }
}


