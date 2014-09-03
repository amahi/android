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
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.FileMetadataRetrievingTask;
import org.amahi.anywhere.util.Mimes;

import java.util.Collections;
import java.util.List;

public class ServerFilesMetadataAdapter extends BaseAdapter
{
	public static final class Tags
	{
		private Tags() {
		}

		public static final int SHARE = R.id.container_files;
		public static final int FILE = R.attr.server_share;

		public static final int FILE_TITLE = R.id.text;
		public static final int FILE_ICON = R.id.icon;
	}

	private final LayoutInflater layoutInflater;

	private final ServerClient serverClient;

	private ServerShare share;
	private List<ServerFile> files;

	public ServerFilesMetadataAdapter(Context context, ServerClient serverClient) {
		this.layoutInflater = LayoutInflater.from(context);

		this.serverClient = serverClient;

		this.files = Collections.emptyList();
	}

	public void replaceWith(ServerShare share, List<ServerFile> files) {
		this.share = share;
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
		View fileView = layoutInflater.inflate(R.layout.view_server_file_metadata_item, container, false);

		fileView.setTag(Tags.FILE_TITLE, fileView.findViewById(R.id.text));
		fileView.setTag(Tags.FILE_ICON, fileView.findViewById(R.id.icon));

		return fileView;
	}

	private void bindView(ServerFile file, View fileView) {
		unbindFileView(file, fileView);

		if (Mimes.match(file.getMime()) != Mimes.Type.VIDEO) {
			bindFileView(file, fileView);
		} else {
			bindFileMetadataView(file, fileView);
		}
	}

	private void unbindFileView(ServerFile file, View fileView) {
		TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
		ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

		fileTitle.setText(null);
		fileTitle.setBackgroundResource(android.R.color.transparent);

		fileIcon.setImageResource(getFileIcon(file));
		fileIcon.setBackgroundResource(R.color.background_secondary);
	}

	@DrawableRes
	private static int getFileIcon(ServerFile file) {
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

	private static void bindFileView(ServerFile file, View fileView) {
		TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
		ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

		fileTitle.setText(file.getName());
		fileTitle.setBackgroundResource(R.color.background_transparent_secondary);

		fileIcon.setImageResource(getFileIcon(file));
		fileIcon.setBackgroundResource(R.color.background_secondary);
	}

	private void bindFileMetadataView(ServerFile file, View fileView) {
		fileView.setTag(Tags.SHARE, share);
		fileView.setTag(Tags.FILE, file);

		FileMetadataRetrievingTask.execute(serverClient, fileView);
	}

	public static void bindView(ServerFile file, ServerFileMetadata fileMetadata, View fileView) {
		if (fileMetadata == null) {
			bindFileView(file, fileView);
		} else {
			bindFileMetadataView(file, fileMetadata, fileView);
		}
	}

	private static void bindFileMetadataView(ServerFile file, ServerFileMetadata fileMetadata, View fileView) {
		TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
		ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

		fileTitle.setText(null);
		fileTitle.setBackgroundResource(android.R.color.transparent);

		Picasso.with(fileView.getContext())
			.load(fileMetadata.getArtworkUrl())
			.centerCrop()
			.fit()
			.placeholder(getFileIcon(file))
			.error(getFileIcon(file))
			.into(fileIcon);
		fileTitle.setBackgroundResource(android.R.color.transparent);
	}
}
