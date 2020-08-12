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

import org.amahi.anywhere.R;
import org.amahi.anywhere.fragment.UploadBottomSheet;
import org.amahi.anywhere.model.UploadOption;

import java.util.ArrayList;

/**
 * Upload options adapter.
 * for the {@link UploadBottomSheet}.
 */
public class UploadOptionsAdapter extends BaseAdapter {
    private ArrayList<UploadOption> uploadOptions;
    private LayoutInflater inflater;

    public UploadOptionsAdapter(Context context, ArrayList<UploadOption> uploadOptions) {
        this.uploadOptions = uploadOptions;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return uploadOptions.size();
    }

    @Override
    public Object getItem(int position) {
        return uploadOptions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        UploadOption uploadOption = uploadOptions.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.upload_list_item, parent, false);
            convertView.setTag(holder);

            holder.image = convertView.findViewById(R.id.option_icon);
            holder.text = convertView.findViewById(R.id.option_text);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setImageResource(uploadOption.getIcon());
        holder.text.setText(uploadOption.getName());

        return convertView;
    }

    static class ViewHolder {
        ImageView image;
        TextView text;
    }
}
