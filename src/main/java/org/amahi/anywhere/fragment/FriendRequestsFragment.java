package org.amahi.anywhere.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendRequestsListAdapter;
import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequest;
import org.amahi.anywhere.server.model.FriendUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Friend Requests Fragment, shows list of friend requests.
 */
public class FriendRequestsFragment extends Fragment {

    @Inject
    ServerClient serverClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        setUpFriendRequestsList();


        showFriendRequests();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private RecyclerView getRecyclerView() {
        return getView().findViewById(R.id.list_friend_requests);
    }

    private void setUpFriendRequestsList() {
        if (serverClient.isConnected()) {
            serverClient.getFriendRequests();
        }
    }

    @Subscribe
    private void onFriendRequestsLoaded(FriendRequestsLoadedEvent event) {
        showFriendRequests();
    }

    private void showFriendRequests() {
        List<FriendRequest> friendRequestsList = new ArrayList<>();

        //TODO: dummy data to be replaced with data from server

        for (int i = 0; i <= 6; i++) {
            FriendRequest friendRequest = new FriendRequest();
            FriendUser friendUser = new FriendUser();
            friendUser.setEmail("dummyuser@dummydomain.com");
            friendRequest.setFriendUser(friendUser);
            friendRequest.setStatus(i % 4);
            friendRequestsList.add(friendRequest);
        }

        FriendRequestsListAdapter adapter = new FriendRequestsListAdapter(getContext(), friendRequestsList);
        getRecyclerView().setAdapter(adapter);
    }

    @Subscribe
    private void onAddFriendUser(AddFriendUserCompletedEvent event) {
        if (event.isSuccessful()) {
            setUpFriendRequestsList();
        }
    }

}
