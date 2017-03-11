package org.amahi.anywhere.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

public class SortingDialogFragment extends DialogFragment {

    private int index=0;

    public static SortingDialogFragment newInstance(int previous_index){
        SortingDialogFragment dialogFragment=new SortingDialogFragment();
        dialogFragment.setIndex(previous_index);

        return dialogFragment;
    }

    public void setIndex(int i){
        index=i;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Sorting Order");

        String[] listItems={"Sort by name A-Z", "Sort by last modified date" };

        builder.setSingleChoiceItems(listItems, index, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                index=i;
            }
        });

        builder.setPositiveButton("Ok", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent=getActivity().getIntent();

                intent.putExtra(ServerFilesFragment.EXTRA_INT_SORT, index);

                getTargetFragment().onActivityResult(
                        getTargetRequestCode(),
                        ServerFilesFragment.DIALOG_RESULT_CODE,
                        intent);

                dismiss();
            }
        });

        builder.setNegativeButton("Cancel", null);

        Dialog dialog=builder.create();

        return dialog;
    }
}
