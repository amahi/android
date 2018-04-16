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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AppSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.ServerApp;

import java.util.Collections;
import java.util.List;

/**
 * Apps adapter. Visualizes web apps
 * for the {@link org.amahi.anywhere.fragment.ServerAppsFragment}.
 */
public class ServerAppsAdapter extends RecyclerView.Adapter<ServerAppsAdapter.ServerAppsViewHolder> {
    private List<ServerApp> apps;
    private Context mContext;

    public ServerAppsAdapter(Context context) {
        mContext = context;
        this.apps = Collections.emptyList();
    }

    @Override
    public ServerAppsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServerAppsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_server_app_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ServerAppsViewHolder holder, int position) {
        holder.text.setText(apps.get(position).getName());
        if (TextUtils.isEmpty(apps.get(position).getLogoUrl()))
            holder.logo.setImageResource(R.drawable.ic_app_logo);
        else {
            Glide
                .with(mContext)
                .load(apps.get(position).getLogoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .placeholder(R.drawable.ic_app_logo)
                .error(R.drawable.ic_app_logo)
                .into(holder.logo);
        }
        holder.itemView.setOnClickListener(view -> BusProvider.getBus().post(new AppSelectedEvent(apps.get(holder.getAdapterPosition()))));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void replaceWith(List<ServerApp> apps) {
        this.apps = apps;

        notifyDataSetChanged();
    }

    public List<ServerApp> getItems() {
        return apps;
    }

    public ServerApp getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ServerAppsViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView logo;

        ServerAppsViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            logo = itemView.findViewById(R.id.logo);
        }
    }
}
