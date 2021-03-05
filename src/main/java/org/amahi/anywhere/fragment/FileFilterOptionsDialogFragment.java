package org.amahi.anywhere.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileFilterOptionClickEvent;
import org.amahi.anywhere.model.FileFilterOption;
import org.amahi.anywhere.util.Preferences;

public class FileFilterOptionsDialogFragment extends BottomSheetDialogFragment {

    private TextView textFilterAllFiles, textFilterDocumentsOnly, textFilterVideosOnly, textFilterAudioOnly, textFilterPhotosOnly;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            assert bottomSheetInternal != null;
            BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(bottomSheetInternal.getHeight());
        });
        return inflater.inflate(R.layout.filefilter_options_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);

        setUpClickListeners();

        setUpSelectedItem();

    }


    private void setUpViews(View view) {

        //Linking operations with UI component on dialog
        textFilterAllFiles = view.findViewById(R.id.text_filter_all_files);

        textFilterDocumentsOnly = view.findViewById(R.id.text_filter_documents_only);

        textFilterVideosOnly = view.findViewById(R.id.text_filter_videos_only);

        textFilterAudioOnly = view.findViewById(R.id.text_filter_audio_only);

        textFilterPhotosOnly = view.findViewById(R.id.text_filter_photos_only);

    }

    private void setUpClickListeners() {

        //Linking particular filtering operation with respective component clicked
        textFilterAllFiles.setOnClickListener(v -> setOptionAndDismiss(FileFilterOption.All));

        textFilterDocumentsOnly.setOnClickListener(v -> setOptionAndDismiss(FileFilterOption.DOCS));

        textFilterVideosOnly.setOnClickListener(v -> setOptionAndDismiss(FileFilterOption.VID));

        textFilterAudioOnly.setOnClickListener(v -> setOptionAndDismiss(FileFilterOption.AUD));

        textFilterPhotosOnly.setOnClickListener(v -> setOptionAndDismiss(FileFilterOption.PICS));
    }

    private void setUpSelectedItem() {

        int filterOption = Preferences.getFilterOption(getContext());

        switch (filterOption) {
            case FileFilterOption.All:
                setItemChecked(textFilterAllFiles);
                break;
            case FileFilterOption.DOCS:
                setItemChecked(textFilterDocumentsOnly);
                break;
            case FileFilterOption.VID:
                setItemChecked(textFilterVideosOnly);
                break;
            case FileFilterOption.AUD:
                setItemChecked(textFilterAudioOnly);
                break;
            case FileFilterOption.PICS:
                setItemChecked(textFilterPhotosOnly);
                break;
        }
    }

    public void setItemChecked(TextView textView) {

        Drawable dw = getResources().getDrawable(R.drawable.ic_check);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, dw, null);
        textView.setTextColor(getResources().getColor(R.color.accent));

    }

    private void setOptionAndDismiss(@FileFilterOption.Types int type) {

        BusProvider.getBus().post(new FileFilterOptionClickEvent(type));
        dismiss();

    }

}
