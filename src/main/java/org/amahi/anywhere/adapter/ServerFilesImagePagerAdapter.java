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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.List;

/**
 * Image files adapter. Maps {@link org.amahi.anywhere.fragment.ServerFileImageFragment}
 * for the {@link org.amahi.anywhere.activity.ServerFileImageActivity}.
 */
public class ServerFilesImagePagerAdapter extends FragmentStatePagerAdapter {
    private final ServerShare share;
    private final List<ServerFile> files;

    public ServerFilesImagePagerAdapter(FragmentManager fragmentManager, ServerShare share, List<ServerFile> files) {
        super(fragmentManager);

        this.share = share;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Fragment getItem(int position) {
        return Fragments.Builder.buildServerFileImageFragment(share, files.get(position));
    }
}
