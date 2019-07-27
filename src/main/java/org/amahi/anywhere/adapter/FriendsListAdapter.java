package org.amahi.anywhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.FriendUserItem;
import org.amahi.anywhere.util.FriendsItemClickListener;

import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsListViewHolder> {
    private List<FriendUserItem> friendsList;
    private Context context;
    private FriendsItemClickListener friendsItemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public FriendsListAdapter(Context context, List<FriendUserItem> friendsList) {
        this.context = context;
        this.friendsList = friendsList;

    }

    public void setOnClickListener(FriendsItemClickListener listener) {
        this.friendsItemClickListener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void removeFriend(int position) {
        this.friendsList.remove(position);
        notifyDataSetChanged();
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

        holder.moreOptions.setOnClickListener(v -> {
            friendsItemClickListener.onMoreOptionClick(holder.itemView, position);

        });

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class FriendsListViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailText;
        TextView createdAt;
        ImageView moreOptions;

        FriendsListViewHolder(View itemView) {
            super(itemView);
            friendEmailText = itemView.findViewById(R.id.text_friend_email);
            createdAt = itemView.findViewById(R.id.text_friend_created_at);
            moreOptions = itemView.findViewById(R.id.friends_more_options);

        }
    }
}


