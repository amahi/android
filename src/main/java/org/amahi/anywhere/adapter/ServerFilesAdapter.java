
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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Mimes;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Files adapter. Visualizes files
 * for the {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesAdapter extends BaseAdapter
{
	private final LayoutInflater layoutInflater;
	private	ServerClient serverClient;

	private ServerShare serverShare;
	private List<ServerFile> files;

	public ServerFilesAdapter(Context context, ServerClient serverClient) {
		this.serverClient = serverClient;
		this.layoutInflater = LayoutInflater.from(context);

		this.files = Collections.emptyList();
	}

	public void replaceWith(ServerShare serverShare, List<ServerFile> files) {
		this.files = files;
		this.serverShare = serverShare;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return files.size();
	}

	public List<ServerFile> getItems() {
		return files;
	}

	@Override
	public ServerFile getItem(int position) {
		return files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup container) {
		ServerFile file = getItem(position);

		if (view == null) {
			view = newView(container);
		}

		bindView(file, view);

		return view;
	}

	private View newView(ViewGroup container) {
		return layoutInflater.inflate(R.layout.view_server_file_item, container, false);
	}

	private void bindView(ServerFile file, View view) {
		ImageView fileIconView = (ImageView) view.findViewById(R.id.icon);
		TextView fileTextView = (TextView) view.findViewById(R.id.text);
		TextView fileSize = (TextView) view.findViewById(R.id.file_size);
		TextView fileLastModified = (TextView) view.findViewById(R.id.last_modified);
               LinearLayout moreInfo = (LinearLayout) view.findViewById(R.id.more_info);

		fileTextView.setText(getFileName(file));

               long size=getFileSize(file);

               if(file.getMime().equals("text/directory")){
                 moreInfo.setVisibility(View.GONE);

               }else{
                 moreInfo.setVisibility(View.VISIBLE);

                 double inMb=(size/1024.0)/1024.0;

                 if(inMb <= 0.1){
                    fileSize.setText(String.format("%.2f", inMb*1024.0)+"KB");
                 }else{
                    fileSize.setText(String.format("%.2f", inMb)+"MB");
                 }

                 Date d=getLastModified(file);
                 SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy");
                 fileLastModified.setText(dt.format(d));
               }

		if (Mimes.match(file.getMime()) == Mimes.Type.IMAGE) {
			setUpImageIcon(file, fileIconView);
		} else {
			fileIconView.setImageResource(getFileIcon(file));
		}
	}

	private String getFileName(ServerFile file) {
		return file.getName();
	}

       private long getFileSize(ServerFile file) {
              return file.getSize();
       }

       private Date getLastModified(ServerFile file) {
              return file.getModificationTime();
       }

	private int getFileIcon(ServerFile file) {
		switch (Mimes.match(file.getMime())) {
			case Mimes.Type.ARCHIVE:
				return R.drawable.ic_file_archive;

			case Mimes.Type.AUDIO:
				return R.drawable.ic_file_audio;

			case Mimes.Type.CODE:
				return R.drawable.ic_file_code;

			case Mimes.Type.DOCUMENT:
				return R.drawable.ic_file_text;

			case Mimes.Type.DIRECTORY:
				return R.drawable.ic_file_directory;

			case Mimes.Type.IMAGE:
				return R.drawable.ic_file_image;

			case Mimes.Type.PRESENTATION:
				return R.drawable.ic_file_presentation;

			case Mimes.Type.SPREADSHEET:
				return R.drawable.ic_file_spreadsheet;

			case Mimes.Type.VIDEO:
				return R.drawable.ic_file_video;

			default:
				return R.drawable.ic_file_generic;
		}
	}

	private void setUpImageIcon(ServerFile file, ImageView fileIconView) {
		Picasso.with(fileIconView.getContext())
				.load(getImageUri(file))
				.centerCrop()
				.fit()
				.placeholder(getFileIcon(file))
				.error(getFileIcon(file))
				.into(fileIconView);
	}

	private Uri getImageUri(ServerFile file) {
		return serverClient.getFileUri(serverShare, file);
	}

}
