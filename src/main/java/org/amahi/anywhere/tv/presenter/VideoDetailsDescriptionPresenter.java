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
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.text.format.Formatter;

import org.amahi.anywhere.server.model.ServerFile;

import java.text.SimpleDateFormat;
import java.util.Date;


public class VideoDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private Context mContext;

    public VideoDetailsDescriptionPresenter(Context context) {
        mContext = context;
    }

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        ServerFile serverFile = (ServerFile) item;
        viewHolder.getTitle().setText(serverFile.getName());
        viewHolder.getSubtitle().setText(getDate(serverFile));
        viewHolder.getBody().setText(getSize(serverFile));
    }

    private String getDate(ServerFile serverFile) {
        Date d = serverFile.getModificationTime();
        SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy");
        return dt.format(d);
    }

    private String getSize(ServerFile serverFile) {
        return Formatter.formatFileSize(mContext, serverFile.getSize());
    }
}
