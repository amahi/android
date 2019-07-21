package org.amahi.anywhere.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileSortOptionClickEvent;
import org.amahi.anywhere.model.FileSortOption;
import org.amahi.anywhere.util.Preferences;

public class FileSortOptionsDialogFragment extends BottomSheetDialogFragment {
    private TextView textSortNameAsc, textSortNameDes, textSortTimeAsc, textSortTimeDes, textSortSizeAsc, textSortSizeDes, textSortFileType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filesort_options_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);

        setUpClickListeners();

        setUpItemSelected();
    }

    private void setUpViews(View view) {
        textSortNameAsc = view.findViewById(R.id.text_sort_name_asc);

        textSortNameDes = view.findViewById(R.id.text_sort_name_des);

        textSortTimeAsc = view.findViewById(R.id.text_sort_time_asc);

        textSortTimeDes = view.findViewById(R.id.text_sort_time_des);

        textSortSizeAsc = view.findViewById(R.id.text_sort_size_asc);

        textSortSizeDes = view.findViewById(R.id.text_sort_size_des);

        textSortFileType = view.findViewById(R.id.text_sort_type);

    }

    private void setUpClickListeners() {
        textSortNameAsc.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.NAME_ASC));

        textSortNameDes.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.NAME_DES));

        textSortTimeAsc.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.TIME_ASC));

        textSortTimeDes.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.TIME_DES));

        textSortSizeAsc.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.SIZE_ASC));

        textSortSizeDes.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.SIZE_DES));

        textSortFileType.setOnClickListener(v -> setOptionAndDismiss(FileSortOption.FILE_TYPE));
    }

    private void setUpItemSelected() {
        int sortOption = Preferences.getSortOption(getContext());

        switch (sortOption) {
            case FileSortOption.NAME_ASC:
                setItemChecked(textSortNameAsc);
                break;
            case FileSortOption.NAME_DES:
                setItemChecked(textSortNameDes);
                break;
            case FileSortOption.TIME_ASC:
                setItemChecked(textSortTimeAsc);
                break;
            case FileSortOption.TIME_DES:
                setItemChecked(textSortTimeDes);
                break;
            case FileSortOption.SIZE_ASC:
                setItemChecked(textSortSizeAsc);
                break;
            case FileSortOption.SIZE_DES:
                setItemChecked(textSortSizeDes);
                break;
            case FileSortOption.FILE_TYPE:
                setItemChecked(textSortFileType);
                break;

        }

    }

    public void setItemChecked(TextView textView) {
        Drawable dw = getResources().getDrawable(R.drawable.ic_check);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, dw, null);
        textView.setTextColor(getResources().getColor(R.color.primary_dark));

    }

    private void setOptionAndDismiss(@FileSortOption.Types int type) {
        BusProvider.getBus().post(new FileSortOptionClickEvent(type));
        dismiss();

    }
}
