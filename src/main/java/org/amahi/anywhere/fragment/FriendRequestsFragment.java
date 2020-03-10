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

import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendRequestsAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequestItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Friend Requests Fragment, shows list of friend requests.
 */
public class FriendRequestsFragment extends Fragment {

    @Inject
    ServerClient serverClient;
    private List<FriendRequestItem> friendRequestsList = new ArrayList<>();

    private ProgressBar progressBarRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

//        setUpFriendRequestsList(savedInstanceState);
         getProgressBarUsersRequests();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private RecyclerView getRecyclerView() {
        return getView().findViewById(R.id.list_friend_requests);
    }

    private ProgressBar getProgressBarUsersRequests(){
        progressBarRequests = (ProgressBar) getView().findViewById(R.id.progress_bar_friendRequests);
        return progressBarRequests;
    }


    private void setUpFriendRequestsList(Bundle state) {
        if (serverClient.isConnected()) {

        }
        setUpListAdapter();


    }

    private FriendRequestsAdapter getListAdapter() {
        return (FriendRequestsAdapter) getRecyclerView().getAdapter();
    }

    private void setUpListAdapter() {
           FriendRequestsAdapter adapter = new FriendRequestsAdapter(getContext(), friendRequestsList);
           getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
           getRecyclerView().setAdapter(adapter);
           progressBarRequests.setVisibility(View.GONE);
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


    @Subscribe
    public void onFriendRequestsLoaded(FriendRequestsLoadedEvent event) {
        friendRequestsList = event.getFriendRequests();
        setUpListAdapter();
    }

}
