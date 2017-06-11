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
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.amahi.anywhere.R;

import java.util.Locale;

public class PercentageView {
    static final int VOLUME = 1;
    static final int BRIGHTNESS = 2;

    @IntDef({VOLUME, BRIGHTNESS})
    @interface TYPE {
    }

    private ViewHolder viewHolder;
    private View view;

    public PercentageView(FrameLayout parentView, @TYPE int type) {
        LayoutInflater inflater = (LayoutInflater) parentView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.percentage_view, parentView, false);
        view.getBackground().setAlpha(100);
        view.requestLayout();
        viewHolder = new ViewHolder(view, type);
    }

    public View getView() {
        return view;
    }

    private class ViewHolder {
        private ProgressBar progressBar;
        private TextView valuePercent, valueName;

        private int type;

        ViewHolder(View itemView, @TYPE int type) {
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            valueName = (TextView) itemView.findViewById(R.id.value_name);
            valuePercent = (TextView) itemView.findViewById(R.id.value_percent);
            this.type = type;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) itemView.getLayoutParams();
            switch (type) {
                case VOLUME:
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    bind("Volume");
                    break;
                case BRIGHTNESS:
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    bind("Brightness");
                    break;
            }
            itemView.setLayoutParams(params);
        }

        private void bind(String name) {
            valueName.setText(name);
        }
    }

    public void setProgress(int n) {
        viewHolder.progressBar.setProgress(n);
        viewHolder.valuePercent.setText(String.format(Locale.getDefault(), "%d %s", n, "%"));
    }
}

