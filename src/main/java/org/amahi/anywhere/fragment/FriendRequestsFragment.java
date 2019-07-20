package org.amahi.anywhere.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendRequestsListAdapter;
import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestDeleteEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequest;
import org.amahi.anywhere.server.model.FriendUser;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.FriendRequestsItemClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Friend Requests Fragment, shows list of friend requests.
 */
public class FriendRequestsFragment extends Fragment implements
    FriendRequestsItemClickListener,
    AlertDialogFragment.DeleteFriendRequestDialogCallback {

    @Inject
    ServerClient serverClient;
    private ProgressDialog deleteProgressDialog;
    List<FriendRequest> friendRequestsList;
    private final static String SELECTED_ITEM = "selected_item";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        setUpFriendRequestsList(savedInstanceState);

        setUpProgressDialog();

    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private RecyclerView getRecyclerView() {
        return getView().findViewById(R.id.list_friend_requests);
    }

    private void setUpFriendRequestsList(Bundle state) {
        setUpItemsMenu();
        /*if (serverClient.isConnected()) {
            serverClient.getFriendRequests();
        }*/
        setUpListAdapter(state);
        setUpListActions();


    }

    private void setUpItemsMenu() {
        setHasOptionsMenu(true);
    }

    private void setUpListActions() {
        getListAdapter().setOnClickListener(this);
    }

    private void setUpListAdapter(Bundle state) {
        friendRequestsList = new ArrayList<>();

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
        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        getRecyclerView().setAdapter(adapter);

        if (isStateValid(state)) {
            getListAdapter().setSelectedPosition(state.getInt(SELECTED_ITEM));
        }
    }

    private FriendRequestsListAdapter getListAdapter() {
        return (FriendRequestsListAdapter) getRecyclerView().getAdapter();
    }

    private boolean isStateValid(Bundle state) {
        return (state != null) && state.containsKey(SELECTED_ITEM);
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
        //setUpListAdapter();
    }

    @Subscribe
    public void onAddFriendUser(AddFriendUserCompletedEvent event) {
        /*if (event.isSuccessful()) {
            setUpListAdapter();
        }*/
    }

    @Subscribe
    public void onDeleteFriendRequest(FriendRequestDeleteEvent event) {
        deleteFriendRequest();
    }

    private void deleteFriendRequest() {
        AlertDialogFragment deleteFriendRequestDialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Fragments.Arguments.DIALOG_TYPE, AlertDialogFragment.DELETE_FRIEND_REQUEST_DIALOG);
        deleteFriendRequestDialog.setArguments(bundle);
        deleteFriendRequestDialog.setTargetFragment(this, 2);
        deleteFriendRequestDialog.show(getFragmentManager(), "delete_friend_request_dialog");
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onMoreOptionClick(View view, int position) {
        getListAdapter().setSelectedPosition(position);
        Fragments.Builder.buildFriendRequestsOptionsDialogFragment()
            .show(getChildFragmentManager(), "friend_requests_options_dialog");


    }

    private void setUpProgressDialog() {
        deleteProgressDialog = new ProgressDialog(getContext());
        deleteProgressDialog.setMessage(getString(R.string.message_delete_progress));
        deleteProgressDialog.setIndeterminate(true);
        deleteProgressDialog.setCancelable(false);
    }


    @Override
    public void dialogPositiveButtonOnClick() {

        int selectedPosition = getListAdapter().getSelectedPosition();
        //show progress dialog and delete data from server
        //deleteProgressDialog.show();
        //serverClient.deleteFriendRequest(friendRequestsList.get(selectedPosition).getId());

        getListAdapter().removeFriend(selectedPosition);
        Toast.makeText(getContext(), "Friend Request deleted successfully", Toast.LENGTH_LONG).show();

    }

    @Override
    public void dialogNegativeButtonOnClick() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM, getListAdapter().getSelectedPosition());
    }
}
