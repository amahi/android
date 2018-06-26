package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.util.Fragments;

public class PrepareDialogFragment extends DialogFragment {

    public static final int PREPARE_DIALOG = 0;
    public static final int DELETE_DIALOG = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prepare_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.main_dialog);


        int dialogType = PREPARE_DIALOG;
        if (getArguments() != null) {
            dialogType = getArguments().getInt(Fragments.Arguments.DIALOG_TYPE);
        }
        if (dialogType == DELETE_DIALOG) {
            textView.setText(R.string.message_delete_progress);
        } else {
            textView.setText(R.string.message_progress_file_preparing);
        }

    }
}
