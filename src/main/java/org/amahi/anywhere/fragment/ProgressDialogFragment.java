package org.amahi.anywhere.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DialogButtonClickedEvent;

/**
 * Dialog Fragment appears in uploading files.
 */

public class ProgressDialogFragment extends DialogFragment {

    public static final String SIGN_IN_DIALOG_TAG = "signing_in";
    public static final String UPLOAD_DIALOG_TAG = "upload_dialog";

    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progress_bar);
        TextView messageText = view.findViewById(R.id.dialog_message);
        TextView cancelText = view.findViewById(R.id.text_view_cancel);

        switch (getTag()) {
            case SIGN_IN_DIALOG_TAG:
                messageText.setText(getString(R.string.message_sign_in_dialog_title));
                break;
            case UPLOAD_DIALOG_TAG:
                messageText.setText(getString(R.string.message_file_upload_title));
                cancelText.setVisibility(View.GONE);
        }

        cancelText.setOnClickListener(v -> {
            BusProvider.getBus().post(new DialogButtonClickedEvent(cancelText.getId()));
        });

    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        return super.onCreateDialog(savedInstanceState);
    }
}
