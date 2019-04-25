package org.amahi.anywhere.util;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

import org.amahi.anywhere.R;

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.custom_edittext_preference);
        setNegativeButtonText("Cancel");
        setPositiveButtonText("OK");

    }

}
