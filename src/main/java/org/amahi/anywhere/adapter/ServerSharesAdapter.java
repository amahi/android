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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.Collections;
import java.util.List;

/**
 * Shares adapter. Visualizes shares
 * for the {@link org.amahi.anywhere.fragment.ServerSharesFragment}.
 */
public class ServerSharesAdapter extends RecyclerView.Adapter<ServerSharesAdapter.ServerShareViewHolder>
{
	private List<ServerShare> shares;

	class ServerShareViewHolder extends RecyclerView.ViewHolder{
		TextView textView;
		ServerShareViewHolder(View itemView) {
			super(itemView);
			textView = (TextView)itemView.findViewById(R.id.text);
		}
	}

	@Override
	public ServerShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ServerShareViewHolder(layoutInflater.inflate(R.layout.view_server_share_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final ServerShareViewHolder holder, int position) {
		holder.textView.setText(shares.get(position).getName());
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BusProvider.getBus().post(new ShareSelectedEvent(shares.get(holder.getAdapterPosition())));
			}
		});
	}

	@Override
	public int getItemCount() {
		return shares.size();
	}

	private final LayoutInflater layoutInflater;

	public ServerSharesAdapter(Context context) {
		this.layoutInflater = LayoutInflater.from(context);

		this.shares = Collections.emptyList();
	}

	public void replaceWith(List<ServerShare> shares) {
		this.shares = shares;

		notifyDataSetChanged();
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
}
