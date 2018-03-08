package org.amahi.anywhere.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import org.amahi.anywhere.R;

/**
 * Dialog Fragment appears in uploading files.
 */

public class ProgressDialogFragment extends DialogFragment {

    private ProgressDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.message_file_upload_title));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return dialog;
    }

    public void setProgress(int progress) {
        dialog.setProgress(progress);
    }
}
