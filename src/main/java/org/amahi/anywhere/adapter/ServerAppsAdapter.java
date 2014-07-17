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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerApp;

import java.util.Collections;
import java.util.List;

public class ServerAppsAdapter extends BaseAdapter
{
	private final LayoutInflater layoutInflater;

	private List<ServerApp> apps;

	public ServerAppsAdapter(Context context) {
		this.layoutInflater = LayoutInflater.from(context);

		this.apps = Collections.emptyList();
	}

	public void replaceWith(List<ServerApp> apps) {
		this.apps = apps;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return apps.size();
	}

	public List<ServerApp> getItems() {
		return apps;
	}

	@Override
	public ServerApp getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup container) {
		ServerApp app = getItem(position);

		if (view == null) {
			view = newView(container);
		}

		bindView(app, view);

		return view;
	}

	private View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_server_app_item, container, false);
	}

	private void bindView(ServerApp app, View view) {
		ImageView appLogoView = (ImageView) view.findViewById(R.id.logo);
		TextView appTextView = (TextView) view.findViewById(R.id.text);

		Picasso
			.with(view.getContext())
			.load(app.getLogoUrl())
			.fit()
			.centerInside()
			.into(appLogoView);

		appTextView.setText(app.getName());
	}
}
