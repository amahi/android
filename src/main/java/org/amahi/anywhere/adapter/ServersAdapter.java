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
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.Server;

import java.util.Collections;
import java.util.List;

public class ServersAdapter extends BaseAdapter
{
	private final LayoutInflater layoutInflater;

	private List<Server> servers;

	public ServersAdapter(Context context) {
		this.layoutInflater = LayoutInflater.from(context);

		this.servers = Collections.emptyList();
	}

	public void replaceWith(List<Server> servers) {
		this.servers = servers;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return servers.size();
	}

	@Override
	public Server getItem(int position) {
		return servers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup container) {
		Server server = getItem(position);

		if (view == null) {
			view = newView(container);
		}

		bindView(server, view);

		return view;
	}

	private View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_list_item, container, false);
	}

	private void bindView(Server server, View view) {
		TextView serverView = (TextView) view;

		serverView.setText(getServerName(server));
		serverView.setCompoundDrawablesWithIntrinsicBounds(getServerStatus(server), 0, 0, 0);
	}

	private String getServerName(Server server) {
		return server.getName();
	}

	private int getServerStatus(Server server) {
		if (server.isActive()) {
			return android.R.drawable.presence_online;
		} else {
			return android.R.drawable.presence_invisible;
		}
	}
}
