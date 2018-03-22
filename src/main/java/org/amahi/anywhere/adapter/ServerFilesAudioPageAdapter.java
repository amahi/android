package org.amahi.anywhere.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.List;

/**
 * Audio files adapter. Maps {@link org.amahi.anywhere.fragment.ServerFileImageFragment}
 * for the {@link org.amahi.anywhere.activity.ServerFileAudioActivity}.
 */

public class ServerFilesAudioPageAdapter extends FragmentStatePagerAdapter {
    private final ServerShare share;
    private final List<ServerFile> files;

    public ServerFilesAudioPageAdapter(FragmentManager fragmentManager, ServerShare share, List<ServerFile> files) {
        super(fragmentManager);
        this.share = share;
        this.files = files;

    }

    @Override
    public Fragment getItem(int position) {
        return Fragments.Builder.buildServerFileAudioFragment(share, files.get(position));
    }

    @Override
    public int getCount() {
        return files.size();
    }
}
