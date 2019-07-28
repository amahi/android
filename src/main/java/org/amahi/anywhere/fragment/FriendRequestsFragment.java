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
import org.amahi.anywhere.bus.DeleteFriendRequestCompletedEvent;
import org.amahi.anywhere.bus.FriendRequestDeleteEvent;
import org.amahi.anywhere.bus.FriendRequestResendEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.bus.ResendFriendRequestCompletedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequestItem;
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
    private ProgressDialog progressDialog;
    private List<FriendRequestItem> friendRequestsList = new ArrayList<>();
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

    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private RecyclerView getRecyclerView() {
        return getView().findViewById(R.id.list_friend_requests);
    }

    private void setUpFriendRequestsList(Bundle state) {
        setUpItemsMenu();
        if (serverClient.isConnected()) {
            serverClient.getFriendRequests();
        }
        setUpListAdapter();
        setUpSelectedItem(state);


    }

    private void setUpItemsMenu() {
        setHasOptionsMenu(true);
    }

    private void setUpListActions() {
        getListAdapter().setOnClickListener(this);
    }

    private void setUpListAdapter() {

        FriendRequestsListAdapter adapter = new FriendRequestsListAdapter(getContext(), friendRequestsList);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        getRecyclerView().setAdapter(adapter);
        setUpListActions();
    }

    private void setUpSelectedItem(Bundle state) {
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
        friendRequestsList = event.getFriendRequests();
        setUpListAdapter();
    }

    @Subscribe
    public void onAddFriendUser(AddFriendUserCompletedEvent event) {
        if (event.isSuccess()) {
            serverClient.getFriendRequests();
        }
    }

    @Subscribe
    public void onDeleteFriendRequest(FriendRequestDeleteEvent event) {
        deleteFriendRequest();
    }

    @Subscribe
    public void onResendFriendRequest(FriendRequestResendEvent event) {
        resendFriendRequest();
    }

    @Subscribe
    public void onDeleteFriendRequestCompleted(DeleteFriendRequestCompletedEvent event) {
        if (event.isSuccess()) {
            getListAdapter().removeFriend(getListAdapter().getSelectedPosition());
        }
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onResendFriendRequestCompleted(ResendFriendRequestCompletedEvent event) {
        if(event.isSuccess()) {
            int position = getListAdapter().getSelectedPosition();

            friendRequestsList.get(position).setStatus(0);
            //set other variables like lastRequestedAt
            getListAdapter().notifyItemChanged(position);
        }
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Toast.makeText(getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void deleteFriendRequest() {
        AlertDialogFragment deleteFriendRequestDialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Fragments.Arguments.DIALOG_TYPE, AlertDialogFragment.DELETE_FRIEND_REQUEST_DIALOG);
        deleteFriendRequestDialog.setArguments(bundle);
        deleteFriendRequestDialog.setTargetFragment(this, 2);
        deleteFriendRequestDialog.show(getFragmentManager(), "delete_friend_request_dialog");
    }

    private void resendFriendRequest() {
        int position = getListAdapter().getSelectedPosition();
        showProgressDialog(getString(R.string.message_resend_progress));
        serverClient.resendFriendRequest(friendRequestsList.get(position).getId());
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

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    //delete friend request dialog listeners
    @Override
    public void dialogPositiveButtonOnClick() {
        int selectedPosition = getListAdapter().getSelectedPosition();
        showProgressDialog(getString(R.string.message_delete_progress));
        serverClient.deleteFriendRequest(friendRequestsList.get(selectedPosition).getId());
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
