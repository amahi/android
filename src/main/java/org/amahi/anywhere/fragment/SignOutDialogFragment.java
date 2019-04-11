package org.amahi.anywhere.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.util.Log;

import org.amahi.anywhere.R;

public class SignOutDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out_title), this::onClick)
            .setNegativeButton(getString(R.string.cancel), this::onClick);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Callback callback =getCallback();

        if (callback != null) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                dialog.dismiss();
                callback.dialogPositiveButtonOnClick();
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                callback.dialogNegativeButtonOnClick();
                dialog.dismiss();
            }
        }

    }

    private Callback getCallback(){
        Callback callback;
        try {
            callback = (Callback) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(this.getClass().getSimpleName(), "Callback of this class must be implemented by target fragment!", e);
            throw e;
        }
        return callback;
    }

    public interface Callback {

        void dialogPositiveButtonOnClick();

        void dialogNegativeButtonOnClick();
    }

}
