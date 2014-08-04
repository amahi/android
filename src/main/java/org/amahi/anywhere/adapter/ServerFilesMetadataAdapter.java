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
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Mimes;

import java.util.Collections;
import java.util.List;

public class ServerFilesMetadataAdapter extends BaseAdapter
{
	private final Context context;
	private final LayoutInflater layoutInflater;

	private List<ServerFile> files;

	public ServerFilesMetadataAdapter(Context context) {
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);

		this.files = Collections.emptyList();
	}

	public void replaceWith(List<ServerFile> files) {
		this.files = files;

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
		return layoutInflater.inflate(R.layout.view_server_file_metadata_item, container, false);
	}

	private void bindView(ServerFile file, View fileView) {
		if (file.getMetadata() == null) {
			bindFileView(file, fileView);
		} else {
			bindFileMetadataView(file, fileView);
		}
	}

	private void bindFileView(ServerFile file, View fileView) {
		ImageView fileIconView = (ImageView) fileView.findViewById(R.id.icon);
		TextView fileTextView = (TextView) fileView.findViewById(R.id.text);

		fileIconView.setImageResource(getFileIcon(file));
		fileTextView.setText(file.getName());
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

	private void bindFileMetadataView(ServerFile file, View fileView) {
		ImageView fileIconView = (ImageView) fileView.findViewById(R.id.icon);
		TextView fileTextView = (TextView) fileView.findViewById(R.id.text);

		Picasso.with(context)
			.load(file.getMetadata().getArtworkUrl())
			.centerCrop()
			.fit()
			.into(fileIconView);

		fileTextView.setText(file.getMetadata().getTitle());
	}
}
