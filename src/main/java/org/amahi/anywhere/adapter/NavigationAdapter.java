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

import java.util.Arrays;
import java.util.List;

/**
 * Navigation adapter. Visualizes predefined values
 * for the {@link org.amahi.anywhere.fragment.NavigationFragment}.
 */
public class NavigationAdapter extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final List<Integer> navigationItems;

    private NavigationAdapter(Context context, List<Integer> navigationItems) {
        this.layoutInflater = LayoutInflater.from(context);

        this.navigationItems = navigationItems;
    }

    public static NavigationAdapter newLocalAdapter(Context context) {
        return new NavigationAdapter(context, Arrays.asList(NavigationItems.SHARES, NavigationItems.APPS));
    }

    public static NavigationAdapter newRemoteAdapter(Context context) {
        return new NavigationAdapter(context, Arrays.asList(NavigationItems.SHARES));
    }

    @Override
    public int getCount() {
        return navigationItems.size();
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

        navigationView.setText(getNavigationName(view.getContext(), navigationItem));
    }

    private String getNavigationName(Context context, int navigationItem) {
        switch (navigationItem) {
            case NavigationItems.SHARES:
                return context.getString(R.string.title_shares);

            case NavigationItems.APPS:
                return context.getString(R.string.title_apps);

            default:
                return null;
        }
    }

    public static final class NavigationItems {
        public static final int SHARES = 0;
        public static final int APPS = 1;
        private NavigationItems() {
        }
    }
}
