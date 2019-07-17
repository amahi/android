package org.amahi.anywhere.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendsPagerAdapter;
import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.fragment.AlertDialogFragment;
import org.amahi.anywhere.fragment.FriendRequestsFragment;
import org.amahi.anywhere.fragment.FriendsFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.NewFriendRequest;
import org.amahi.anywhere.util.Fragments;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FriendsActivity extends AppCompatActivity
    implements AlertDialogFragment.AddFriendDialogCallback {

    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setUpHomeNavigation();

        setUpInjections();

        setUpPager();

        getTabLayout().setupWithViewPager(getPager());

        setUpAddFriendFAB();

    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
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

    private void setUpAddFriendFAB() {
        FloatingActionButton fab = findViewById(R.id.fab_add_friend);

        fab.setOnClickListener(v -> showAddFriendDialog());
    }

    private void showAddFriendDialog() {
        AlertDialogFragment addFriendDialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Fragments.Arguments.DIALOG_TYPE, AlertDialogFragment.ADD_FRIEND_DIALOG);
        addFriendDialog.setArguments(bundle);
        addFriendDialog.show(getSupportFragmentManager(), "add_friend_dialog");

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


    @Override
    public void dialogPositiveButtonOnClick(String email) {
        if (serverClient.isConnected()) {
            addFriendRequest(email);
        }
        //TODO: connect server if not connected
    }

    @Override
    public void dialogNegativeButtonOnClick() {

    }

    private void addFriendRequest(String email) {

        if (email != null) {
            NewFriendRequest newFriendRequest = new NewFriendRequest();
            newFriendRequest.setEmail(email);
            serverClient.addFriendUser(newFriendRequest);
        }

    }


    @Subscribe
    private void onAddFriendUser(AddFriendUserCompletedEvent event) {
        if (event.isSuccessful()) {
            Toast.makeText(this, "friend request success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "friend request failed", Toast.LENGTH_LONG).show();
        }

    }
}
