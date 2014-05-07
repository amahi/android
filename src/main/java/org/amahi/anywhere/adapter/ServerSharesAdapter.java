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
import org.amahi.anywhere.server.model.ServerShare;

import java.util.Collections;
import java.util.List;

public class ServerSharesAdapter extends BaseAdapter
{
	private final LayoutInflater layoutInflater;

	private List<ServerShare> shares;

	public ServerSharesAdapter(Context context) {
		this.layoutInflater = LayoutInflater.from(context);

		this.shares = Collections.emptyList();
	}

	public void replaceWith(List<ServerShare> shares) {
		this.shares = shares;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return shares.size();
	}

	@Override
	public ServerShare getItem(int position) {
		return shares.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup container) {
		ServerShare share = getItem(position);

		if (view == null) {
			view = newView(container);
		}

		bindView(share, view);

		return view;
	}

	private View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_list_item, container, false);
	}

	private void bindView(ServerShare share, View view) {
		TextView shareView = (TextView) view;

		shareView.setText(getShareName(share));
	}

	private String getShareName(ServerShare share) {
		return share.getName();
	}
}
