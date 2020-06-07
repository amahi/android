package org.amahi.anywhere.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.FriendUserItem;

import java.util.List;

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.FriendsListViewHolder> {
    private List<FriendUserItem> friendsList;
    private Context context;

    public FriendsRecyclerViewAdapter(Context context, List<FriendUserItem> friendsList) {
        this.context = context;
        this.friendsList = friendsList;

    }

    @NonNull
    @Override
    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendsListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {
        holder.friendEmailText.setText(friendsList.get(position).getEmail());
        StringBuilder builder = new StringBuilder();
        builder.append("Friends since ");
        builder.append(friendsList.get(position).getCreatedAt());
        holder.createdAt.setText(builder);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class FriendsListViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailText;
        TextView createdAt;

        FriendsListViewHolder(View itemView) {
            super(itemView);
            friendEmailText = itemView.findViewById(R.id.text_friend_email);
            createdAt = itemView.findViewById(R.id.text_friend_created_at);

        }
    }

}


