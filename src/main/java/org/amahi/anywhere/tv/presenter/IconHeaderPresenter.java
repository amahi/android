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

package org.amahi.anywhere.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.amahi.anywhere.R;

public class IconHeaderPresenter extends RowHeaderPresenter {

    private float mUnselectedAlpha;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mUnselectedAlpha = parent.getResources()
            .getFraction(R.fraction.lb_browse_header_unselect_alpha, 1, 1);

        LayoutInflater inflater = (LayoutInflater) parent.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.tv_header_item, null);
        view.setAlpha(mUnselectedAlpha);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        HeaderItem headerItem = ((ListRow) item).getHeaderItem();
        View rootView = viewHolder.view;
        rootView.setFocusable(true);
        ImageView imageView = rootView.findViewById(R.id.header_icon);

        if (headerItem.getName().matches("Settings")) {
            imageView.setVisibility(View.VISIBLE);
            Drawable icon = ContextCompat.getDrawable(rootView.getContext(), R.drawable.ic_menu_settings);
            imageView.setImageDrawable(icon);
        } else {
            imageView.setVisibility(View.GONE);
            TextView label = rootView.findViewById(R.id.header_label);
            label.setTextColor(Color.WHITE);
            label.setText(headerItem.getName());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        // Nothing to be done here.
    }

    @Override
    protected void onSelectLevelChanged(RowHeaderPresenter.ViewHolder holder) {
        holder.view.setAlpha(mUnselectedAlpha + holder.getSelectLevel() *
            (1.0f - mUnselectedAlpha));
    }
}
