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

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsListViewHolder> {
    private List<String> friendsEmailList;
    private Context context;

    public FriendsListAdapter(Context context, List<String> friendsEmailList) {
        this.context = context;
        this.friendsEmailList = friendsEmailList;

    }

    @NonNull
    @Override
    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendsListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {
        holder.friendEmailText.setText(friendsEmailList.get(position));

    }

    @Override
    public int getItemCount() {
        return friendsEmailList.size();
    }

    class FriendsListViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailText;
        FriendsListViewHolder(View itemView) {
            super(itemView);
            friendEmailText = itemView.findViewById(R.id.text_friend_email);

        }
    }
}


