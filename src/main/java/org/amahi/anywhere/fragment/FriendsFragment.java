package org.amahi.anywhere.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

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
import org.amahi.anywhere.adapter.FriendsListAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DeleteFriendCompletedEvent;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.bus.FriendDeleteEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendUserItem;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.FriendsItemClickListener;

import java.util.ArrayList;

import java.util.List;

import javax.inject.Inject;

/**
 * Friends Fragment, shows list of friended users.
 */
public class FriendsFragment extends Fragment implements
    FriendsItemClickListener,
    AlertDialogFragment.DeleteFriendDialogCallback {

    @Inject
    ServerClient serverClient;
    private ProgressDialog deleteProgressDialog;
    private List<FriendUserItem> friendsList = new ArrayList<>();
    private final static String SELECTED_ITEM = "selected_item";

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

        setUpFriendsList(savedInstanceState);

        setUpProgressDialog();


    }

    private RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(R.id.list_friends);
    }

    private void setUpFriendsList(Bundle state) {
        setUpItemsMenu();
        if (serverClient.isConnected()) {
            serverClient.getFriendUsers();
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

    private FriendsListAdapter getListAdapter() {
        return (FriendsListAdapter) getRecyclerView().getAdapter();
    }


    private void setUpListAdapter() {

        FriendsListAdapter adapter = new FriendsListAdapter(getContext(), friendsList);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        getRecyclerView().setAdapter(adapter);

        setUpListActions();
    }

    private void setUpSelectedItem(Bundle state) {
        if (isStateValid(state)) {
            getListAdapter().setSelectedPosition(state.getInt(SELECTED_ITEM));
        }
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
    public void onFriendUsersLoaded(FriendUsersLoadedEvent event) {
        friendsList = event.getFriendUsers();
        setUpListAdapter();
    }

    @Subscribe
    public void onDeleteFriend(FriendDeleteEvent event) {
        deleteFriend();

    }

    @Subscribe
    public void onDeleteFriendCompleted(DeleteFriendCompletedEvent event) {

        if (event.isSuccess()) {
            getListAdapter().removeFriend(getListAdapter().getSelectedPosition());
        }
        if (deleteProgressDialog.isShowing()) {
            deleteProgressDialog.dismiss();
        }
        Toast.makeText(getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void deleteFriend() {
        AlertDialogFragment deleteFriendDialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Fragments.Arguments.DIALOG_TYPE, AlertDialogFragment.DELETE_FRIEND_DIALOG);
        deleteFriendDialog.setArguments(bundle);
        deleteFriendDialog.setTargetFragment(this, 2);
        deleteFriendDialog.show(getFragmentManager(), "delete_friend_dialog");
    }


    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onMoreOptionClick(View view, int position) {
        getListAdapter().setSelectedPosition(position);
        Fragments.Builder.buildFriendsOptionsDialogFragment()
            .show(getChildFragmentManager(), "friends_options_dialog");


    }

    private void setUpProgressDialog() {
        deleteProgressDialog = new ProgressDialog(getContext());
        deleteProgressDialog.setMessage(getString(R.string.message_delete_progress));
        deleteProgressDialog.setIndeterminate(true);
        deleteProgressDialog.setCancelable(false);
    }

    //delete friend confirmation dialog callback methods
    @Override
    public void dialogPositiveButtonOnClick() {
        int selectedPosition = getListAdapter().getSelectedPosition();
        //delete data from server and show progress dialog
        deleteProgressDialog.show();
        serverClient.deleteFriendUser(friendsList.get(selectedPosition).getId());

    }

    @Override
    public void dialogNegativeButtonOnClick() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_ITEM, getListAdapter().getSelectedPosition());
    }

}
