/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.l4digital.fastscroll.FastScroller;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Constants;

import java.util.Collections;
import java.util.List;

/**
 * Shares adapter. Visualizes shares
 * for the {@link org.amahi.anywhere.fragment.ServerSharesFragment}.
 */
public class ServerSharesAdapter extends RecyclerView.Adapter<ServerSharesAdapter.ServerShareViewHolder> implements FastScroller.SectionIndexer {
    private List<ServerShare> shares;
    public boolean showShimmer = true;

    public ServerSharesAdapter(Context context) {
        this.shares = Collections.emptyList();
    }

    @Override
    public ServerShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServerShareViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_server_share_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ServerShareViewHolder holder, int position) {
        if (showShimmer) {
            holder.shimmerFrameLayout.startShimmer();
        } else {
            stopShimmer(holder);
            holder.textView.setText(shares.get(position).getName());
            holder.imageView.setImageResource(R.drawable.ic_right_arrow_24dp);
            holder.itemView.setOnClickListener(view -> BusProvider.getBus().post(new ShareSelectedEvent(shares.get(holder.getAdapterPosition()))));
        }
    }

    private void stopShimmer(ServerShareViewHolder holder) {
        holder.shimmerFrameLayout.stopShimmer();
        holder.shimmerFrameLayout.setShimmer(null);
        holder.textView.setBackground(null);
        holder.imageView.setBackground(null);
    }

    @Override
    public int getItemCount() {
        if (showShimmer) {
            return Constants.SHIMMER_ITEM_NUMBER;
        } else {
            return shares.size();
        }
    }

    public void replaceWith(List<ServerShare> shares) {
        this.shares = shares;

        notifyDataSetChanged();
    }

    @Override
    public CharSequence getSectionText(int selectedPosition) {
        return getItem(selectedPosition).getName().subSequence(0, 1);
    }

    public List<ServerShare> getItems() {
        return shares;
    }

    public ServerShare getItem(int position) {
        return shares.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ServerShareViewHolder extends RecyclerView.ViewHolder {
        ShimmerFrameLayout shimmerFrameLayout;
        TextView textView;
        ImageView imageView;

        ServerShareViewHolder(View itemView) {
            super(itemView);
            shimmerFrameLayout = itemView.findViewById(R.id.shimmer_layout_file);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.right_arrow);
        }
    }
}
