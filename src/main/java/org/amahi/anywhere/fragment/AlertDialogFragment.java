package org.amahi.anywhere.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.app.AlertDialog;

import androidx.fragment.app.DialogFragment;

import android.util.Log;

import org.amahi.anywhere.R;
import org.amahi.anywhere.util.Fragments;

import java.io.File;

public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    File file;
    private int dialogType = -1;
    AlertDialog.Builder builder;
    public static final int DELETE_FILE_DIALOG = 0;
    public static final int DUPLICATE_FILE_DIALOG = 1;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new android.app.AlertDialog.Builder(getActivity());

        if (getArguments() != null) {
            dialogType = getArguments().getInt(Fragments.Arguments.DIALOG_TYPE);
        }

        switch (dialogType) {
            case DELETE_FILE_DIALOG:
                buildDeleteDialog();
                break;

            case DUPLICATE_FILE_DIALOG:
                buildDuplicateDialog();
                break;

        }

        return builder.create();
    }

    private void buildDeleteDialog() {
        builder.setTitle(getString(R.string.message_delete_file_title))
            .setMessage(getString(R.string.message_delete_file_body))
            .setPositiveButton(getString(R.string.button_yes), this)
            .setNegativeButton(getString(R.string.button_no), this);
    }

    private void buildDuplicateDialog() {
        file = (File) getArguments().getSerializable("file");
        builder.setTitle(getString(R.string.message_duplicate_file_upload))
            .setMessage(getString(R.string.message_duplicate_file_upload_body, file.getName()))
            .setPositiveButton(getString(R.string.button_yes), this)
            .setNegativeButton(getString(R.string.button_no), this);
    }



    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (dialogType == DUPLICATE_FILE_DIALOG) {
            DuplicateFileDialogCallback callback = getDuplicateDialogCallback();

            if (callback != null) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    callback.dialogPositiveButtonOnClick(file);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.dismiss();
                    callback.dialogNegativeButtonOnClick();
                }
            }

        } else if (dialogType == DELETE_FILE_DIALOG) {
            DeleteFileDialogCallback callback = getDeleteDialogCallback();

            if (callback != null) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    callback.dialogPositiveButtonOnClick();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.dismiss();
                    callback.dialogNegativeButtonOnClick();
                }
            }
        }


    }

    private DuplicateFileDialogCallback getDuplicateDialogCallback() {
        DuplicateFileDialogCallback callback;
        if (getTargetFragment() != null) {
            try {
                callback = (DuplicateFileDialogCallback) getTargetFragment();
            } catch (ClassCastException e) {
                Log.e(this.getClass().getSimpleName(), "Callback of this class must be implemented by target fragment!", e);
                throw e;
            }
        } else {
            try {
                callback = (DuplicateFileDialogCallback) getActivity();
            } catch (ClassCastException e) {
                Log.e(this.getClass().getSimpleName(), "Callback of this class must be implemented by the activity!", e);
                throw e;
            }
        }
        return callback;
    }

    private DeleteFileDialogCallback getDeleteDialogCallback() {
        DeleteFileDialogCallback callback;
        if (getTargetFragment() != null) {
            try {
                callback = (DeleteFileDialogCallback) getTargetFragment();
            } catch (ClassCastException e) {
                Log.e(this.getClass().getSimpleName(), "Callback of this class must be implemented by target fragment!", e);
                throw e;
            }
        } else {
            try {
                callback = (DeleteFileDialogCallback) getActivity();
            } catch (ClassCastException e) {
                Log.e(this.getClass().getSimpleName(), "Callback of this class must be implemented by the activity!", e);
                throw e;
            }
        }
        return callback;
    }


    public interface DuplicateFileDialogCallback {

        void dialogPositiveButtonOnClick(File file);


        void dialogNegativeButtonOnClick();
    }

    public interface DeleteFileDialogCallback {

        void dialogPositiveButtonOnClick();


        void dialogNegativeButtonOnClick();
    }

}
