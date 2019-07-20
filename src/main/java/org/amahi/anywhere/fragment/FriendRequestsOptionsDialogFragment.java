package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestDeleteEvent;

public class FriendRequestsOptionsDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_requests_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);
    }

    private void setUpViews(View view) {

        LinearLayout deleteLayout = view.findViewById(R.id.friend_requests_delete_layout);

        deleteLayout.setOnClickListener(v -> setUpDelete());
    }

    private void setUpDelete() {
        BusProvider.getBus().post(new FriendRequestDeleteEvent());
        dismiss();
    }
}
