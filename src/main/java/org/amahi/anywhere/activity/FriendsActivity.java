package org.amahi.anywhere.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendsPagerAdapter;
import org.amahi.anywhere.fragment.FriendRequestsFragment;
import org.amahi.anywhere.fragment.FriendsFragment;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setUpHomeNavigation();

        setUpPager();

        getTabLayout().setupWithViewPager(getPager());

    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpPager() {

        List<Fragment> fragmentList = getFragments();

        List<String> fragmentTitles = getFragmentTitles();

        FriendsPagerAdapter adapter = new FriendsPagerAdapter(getSupportFragmentManager(), fragmentList, fragmentTitles);
        getPager().setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private List<Fragment> getFragments() {

        Fragment friendsFragment = new FriendsFragment();
        Fragment friendRequestsFragment = new FriendRequestsFragment();
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(friendsFragment);
        fragmentList.add(friendRequestsFragment);

        return fragmentList;
    }

    private List<String> getFragmentTitles() {

        List<String> fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Friends");
        fragmentTitles.add("Friend Requests");

        return fragmentTitles;
    }

    private ViewPager getPager() {
        return findViewById(R.id.pager_friends);
    }

    private TabLayout getTabLayout() {
        return findViewById(R.id.tablayout_friends);
    }


}
