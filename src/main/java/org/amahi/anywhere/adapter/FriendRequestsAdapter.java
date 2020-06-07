package org.amahi.anywhere.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.FriendRequestItem;

import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestsViewHolder> {
    private List<FriendRequestItem> friendRequestsList;
    private Context context;

    public FriendRequestsAdapter(Context context, List<FriendRequestItem> friendRequestsList) {
        this.context = context;
        this.friendRequestsList = friendRequestsList;

    }


    @NonNull
    @Override
    public FriendRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_requests_list_item, parent, false);
        FriendRequestsViewHolder friendRequestsViewHolder = new FriendRequestsViewHolder(view);

         return friendRequestsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsViewHolder holder, int position) {
        holder.friendEmailText.setText(friendRequestsList.get(position).getEmail());
        String status = friendRequestsList.get(position).getStatusTxt();

        holder.friendRequestStatus.setText(status);

    }

    @Override
    public int getItemCount() {
        return friendRequestsList.size();
    }

    class FriendRequestsViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailText;
        TextView friendRequestStatus;
        LinearLayout friendsRequestLinearLayout;
        FriendRequestsViewHolder(View itemView) {
            super(itemView);
            friendsRequestLinearLayout = itemView.findViewById(R.id.friends_request_linear_layout);
            friendEmailText = itemView.findViewById(R.id.text_friend_request_email);
            friendRequestStatus = itemView.findViewById(R.id.text_friend_request_status);
        }
    }


}



