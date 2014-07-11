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

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.List;

public class ServerFileImagePagerAdapter extends FragmentStatePagerAdapter
{
	private final ServerShare imageShare;
	private final List<ServerFile> imageFiles;

	public ServerFileImagePagerAdapter(FragmentManager fragmentManager, ServerShare imageShare, List<ServerFile> imageFiles) {
		super(fragmentManager);

		this.imageShare = imageShare;
		this.imageFiles = imageFiles;
	}

	@Override
	public int getCount() {
		return imageFiles.size();
	}

	@Override
	public Fragment getItem(int imagePosition) {
		return Fragments.Builder.buildServerFileImageFragment(imageShare, imageFiles.get(imagePosition));
	}
}
