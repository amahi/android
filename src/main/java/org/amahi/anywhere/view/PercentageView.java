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
package org.amahi.anywhere.view;

/*
  PercentageView.
  Custom view class for displaying volume and brightness controls on screen while using Swipe Gestures.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.amahi.anywhere.R;

import java.util.Locale;

public class PercentageView {
    public static final int VOLUME = 1;
    public static final int BRIGHTNESS = 2;
    private ViewHolder viewHolder;
    private View view;

    public PercentageView(FrameLayout parentView) {
        LayoutInflater inflater = (LayoutInflater) parentView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.percentage_view, parentView, false);
        view.requestLayout();
        viewHolder = new ViewHolder(view);
    }

    public View getView() {
        return view;
    }

    public void setType(@TYPE int type) {
        switch (type) {
            case VOLUME:
                viewHolder.icon.setImageResource(R.drawable.ic_volume_up);
                viewHolder.progressBar.getProgressDrawable().setColorFilter(
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            case BRIGHTNESS:
                viewHolder.icon.setImageResource(R.drawable.ic_brightness);
                viewHolder.progressBar.getProgressDrawable().setColorFilter(
                    Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    public void setProgress(int n) {
        viewHolder.progressBar.setProgress(n);
        viewHolder.valuePercent.setText(String.format(Locale.getDefault(), "%d %s", n, "%"));
    }

    public void hide() {
        view.setVisibility(View.GONE);
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
    }

    public boolean isShowing() {
        return view.getVisibility() == View.VISIBLE;
    }

    @IntDef({VOLUME, BRIGHTNESS})
    @interface TYPE {
    }

    private class ViewHolder {
        private ProgressBar progressBar;
        private TextView valuePercent;
        private ImageView icon;

        ViewHolder(View itemView) {
            progressBar = itemView.findViewById(R.id.progress_bar);
            icon = itemView.findViewById(R.id.type_icon);
            valuePercent = itemView.findViewById(R.id.value_percent);
        }
    }
}

