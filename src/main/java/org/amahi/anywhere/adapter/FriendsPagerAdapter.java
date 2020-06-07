package org.amahi.anywhere.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class FriendsPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragmentList;
    private List<String> fragmentTitles;

    public FriendsPagerAdapter(FragmentManager fm, List<Fragment> fragmentList,
                               List<String> fragmentTitles) {
        super(fm);
        this.fragmentList = fragmentList;
        this.fragmentTitles = fragmentTitles;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
