
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Mimes;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

/**
 * Files adapter. Visualizes files
 * for the {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesAdapter extends FilesFilterBaseAdapter
{
	private Context context;

	public ServerFilesAdapter(Context context, ServerClient serverClient) {
		this.serverClient = serverClient;
		this.layoutInflater = LayoutInflater.from(context);
		this.context=context;
		this.files = Collections.emptyList();
		this.filteredFiles = Collections.emptyList();
	}

	protected View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_server_file_item, container, false);
	}

	protected void bindView(ServerFile file, View view) {
		ImageView fileIconView = (ImageView) view.findViewById(R.id.icon);
		TextView fileTextView = (TextView) view.findViewById(R.id.text);
		TextView fileSize = (TextView) view.findViewById(R.id.file_size);
		TextView fileLastModified = (TextView) view.findViewById(R.id.last_modified);
		LinearLayout moreInfo = (LinearLayout) view.findViewById(R.id.more_info);

		if(Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY){
			moreInfo.setVisibility(View.GONE);

		} else {
			moreInfo.setVisibility(View.VISIBLE);

			fileSize.setText(Formatter.formatFileSize(context, getFileSize(file)));

			Date d = getLastModified(file);
			SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy");
			fileLastModified.setText(dt.format(d));
		}

		SpannableStringBuilder sb = new SpannableStringBuilder(file.getName());
		if(queryString != null && !TextUtils.isEmpty(queryString)) {
			int searchMatchPosition = file.getName().toLowerCase().indexOf(queryString.toLowerCase());
			if (searchMatchPosition != -1)
				sb.setSpan(fcs, searchMatchPosition, searchMatchPosition + queryString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		fileTextView.setText(sb);

		if (Mimes.match(file.getMime()) == Mimes.Type.IMAGE) {
			setUpImageIcon(file, fileIconView);
		} else {
			fileIconView.setImageResource(getFileIcon(file));
		}
	}

	private long getFileSize(ServerFile file) {
		return file.getSize();
	}

	private Date getLastModified(ServerFile file) {
		return file.getModificationTime();
	}
}
