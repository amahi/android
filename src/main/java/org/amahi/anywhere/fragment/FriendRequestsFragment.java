package org.amahi.anywhere.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendRequestsListAdapter;
import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequest;
import org.amahi.anywhere.server.model.NewFriendRequest;

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

        Button btnAddFR = getView().findViewById(R.id.btn_add_fr);

        btnAddFR.setOnClickListener(v -> addFriendRequest());
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
        showFriendRequests(event.getFriendRequests());
    }

    private void showFriendRequests(List<FriendRequest> friendRequests) {
        List<String> friendRequestsEmailList = new ArrayList<>();

        for (FriendRequest friendRequest : friendRequests) {
            friendRequestsEmailList.add(friendRequest.getFriendUser().getEmail());
        }

        FriendRequestsListAdapter adapter = new FriendRequestsListAdapter(getContext(), friendRequestsEmailList);
        getRecyclerView().setAdapter(adapter);
    }

    private void addFriendRequest() {
        TextInputEditText etEmailFR = getView().findViewById(R.id.text_email);

        if (etEmailFR != null && etEmailFR.getText() != null) {
            NewFriendRequest newFriendRequest = new NewFriendRequest();
            newFriendRequest.setEmail(etEmailFR.getText().toString());
            serverClient.addFriendUser(newFriendRequest);
        }

    }

    @Subscribe
    private void onAddFriendUser(AddFriendUserCompletedEvent event) {
        if (event.isSuccessful()) {
            Toast.makeText(getContext(), "friend request success", Toast.LENGTH_LONG).show();
            setUpFriendRequestsList();
        } else {
            Toast.makeText(getContext(), "friend request failed", Toast.LENGTH_LONG).show();
        }

    }

}
