package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOptionClickEvent;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

public class FileOptionsDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_options, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);
    }

    private void setUpViews(View view) {

        LinearLayout shareLayout = view.findViewById(R.id.share_layout);
        LinearLayout deleteLayout = view.findViewById(R.id.delete_layout);
        LinearLayout downloadLayout = view.findViewById(R.id.download_layout);
        LinearLayout offlineLayout = view.findViewById(R.id.offline_layout);
        LinearLayout fileInfoLayout = view.findViewById(R.id.file_info_layout);

        String uniqueKey = getFileUniqueKey();

        shareLayout.setOnClickListener(v -> setOptionAndDismiss(FileOption.SHARE, uniqueKey));
        deleteLayout.setOnClickListener(v -> setOptionAndDismiss(FileOption.DELETE, uniqueKey));
        downloadLayout.setOnClickListener(v -> setOptionAndDismiss(FileOption.DOWNLOAD, uniqueKey));
        fileInfoLayout.setOnClickListener(v -> setOptionAndDismiss(FileOption.FILE_INFO, uniqueKey));

        if (!isOfflineFragment()) {
            SwitchCompat offlineSwitch = view.findViewById(R.id.offline_switch);

            ServerFile file = getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
            if (Intents.Builder.with(getContext()).isMediaServerFile(file)) {
                offlineSwitch.setChecked(file.isOffline());

                offlineSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        setOption(FileOption.OFFLINE_ENABLED, uniqueKey);
                    } else {
                        setOption(FileOption.OFFLINE_DISABLED, uniqueKey);
                    }
                    file.setOffline(b);
                    getArguments().putParcelable(Fragments.Arguments.SERVER_FILE, file);
                });
            } else {
                offlineLayout.setVisibility(View.GONE);
            }
        } else {
            offlineLayout.setVisibility(View.GONE);
        }
    }

    private String getFileUniqueKey() {
        ServerFile file = getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
        return file.getUniqueKey();
    }

    private boolean isOfflineFragment() {
        return getArguments().getBoolean(Fragments.Arguments.IS_OFFLINE_FRAGMENT);
    }

    public void setOption(@FileOption.Types int type, String uniqueKey) {
        BusProvider.getBus().post(new FileOptionClickEvent(type, uniqueKey));
    }

    public void setOptionAndDismiss(@FileOption.Types int type, String uniqueKey) {
        setOption(type, uniqueKey);
        dismiss();
    }
}
