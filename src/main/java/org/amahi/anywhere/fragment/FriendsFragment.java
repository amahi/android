package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendsRecyclerViewAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.bus.ServerConnectionFailedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendUserItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Friends Fragment, shows list of friended users.
 */
public class FriendsFragment extends Fragment {

    @Inject
    ServerClient serverclient;

    private List<FriendUserItem> friendsList = new ArrayList<>();
    private ProgressBar progressBarUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_friends, container, false);

    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

//        setUpFriendsList(savedInstanceState);
         getProgressBarUsers();

    }

    @Subscribe
    public void onFriendUsersLoaded(FriendUsersLoadedEvent event) {
        friendsList = event.getFriendUsers();
        setUpListAdapter();
    }


    private RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(R.id.list_friends);
    }

    private ProgressBar getProgressBarUsers(){
        progressBarUsers = (ProgressBar) getView().findViewById(R.id.progress_bar_friendUsers);
        return progressBarUsers;
    }


    private void setUpFriendsList(Bundle state) {
        if (serverclient.isConnected()) {

        }

        setUpListAdapter();
    }


    private FriendsRecyclerViewAdapter getListAdapter() {
        return (FriendsRecyclerViewAdapter) getRecyclerView().getAdapter();
    }


    private void setUpListAdapter() {

            FriendsRecyclerViewAdapter adapter = new FriendsRecyclerViewAdapter(getContext(), friendsList);
            getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            getRecyclerView().setAdapter(adapter);
            progressBarUsers.setVisibility(View.GONE);
            getRecyclerView().setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }


}
