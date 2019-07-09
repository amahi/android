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
import org.amahi.anywhere.adapter.FriendsListAdapter;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.PrimaryUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Friends Fragment, shows list of friended users.
 */
public class FriendsFragment extends Fragment {

    @Inject
    ServerClient serverClient;

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

        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        setUpFriendsList();
    }

    private RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(R.id.list_friends);
    }

    private void setUpFriendsList() {
        serverClient.getFriendUsers();

    }

    @Subscribe
    private void onFriendUsersLoaded(FriendUsersLoadedEvent event) {
        showFriendUsers(event.getPrimaryUsers());
    }

    private void showFriendUsers(List<PrimaryUser> primaryUsers) {
        List<String> friendsEmailList = new ArrayList<>();

        for (PrimaryUser primaryUser : primaryUsers) {
            friendsEmailList.add(primaryUser.getFriendUser().getEmail());
        }

        FriendsListAdapter adapter = new FriendsListAdapter(getContext(), friendsEmailList);
        getRecyclerView().setAdapter(adapter);

    }


}
