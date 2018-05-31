package org.amahi.anywhere.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DialogButtonClickedEvent;

public class ResumeDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder =
            new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.message_resume_last_position))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    BusProvider.getBus().post(new DialogButtonClickedEvent(DialogButtonClickedEvent.YES));
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    BusProvider.getBus().post(new DialogButtonClickedEvent(DialogButtonClickedEvent.NO));
                });

        setCancelable(false);

        return dialogBuilder.create();
    }
}
