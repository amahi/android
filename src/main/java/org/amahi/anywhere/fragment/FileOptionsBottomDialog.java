package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOptionClickEvent;
import org.amahi.anywhere.model.FileOption;

public class FileOptionsBottomDialog extends BottomSheetDialogFragment {

    public static FileOptionsBottomDialog newInstance() {
        return new FileOptionsBottomDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_options, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout shareLayout = view.findViewById(R.id.share_layout);
        LinearLayout downloadLayout = view.findViewById(R.id.download_layout);
        LinearLayout deleteLayout = view.findViewById(R.id.delete_layout);

        shareLayout.setOnClickListener(v -> setOption(FileOption.SHARE));
        downloadLayout.setOnClickListener(v -> setOption(FileOption.DOWNLOAD));
        deleteLayout.setOnClickListener(v -> setOption(FileOption.DELETE));
    }

    public void setOption(@FileOption.Types int type) {
        BusProvider.getBus().post(new FileOptionClickEvent(type));
        dismiss();
    }
}
