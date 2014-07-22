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

public class NavigationAdapter extends BaseAdapter
{
	public static final class NavigationItems
	{
		private NavigationItems() {
		}

		public static final int SHARES = 0;
		public static final int APPS = 1;

		private static final int COUNT = 1;
	}

	private final LayoutInflater layoutInflater;

	public NavigationAdapter(Context context) {
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return NavigationItems.COUNT;
	}

	@Override
	public Integer getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup container) {
		if (view == null) {
			view = newView(container);
		}

		bindView(position, view);

		return view;
	}

	private View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_navigation_item, container, false);
	}

	private void bindView(int navigationItem, View view) {
		TextView navigationView = (TextView) view;

		navigationView.setText(getNavigationName(navigationItem));
	}

	private String getNavigationName(int navigationItem) {
		switch (navigationItem) {
			case NavigationItems.SHARES:
				return "Shares";

			case NavigationItems.APPS:
				return "Apps";

			default:
				return null;
		}
	}
}
