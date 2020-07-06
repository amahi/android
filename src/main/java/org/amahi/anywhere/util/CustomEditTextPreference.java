package org.amahi.anywhere.util;

import android.content.Context;

import androidx.preference.EditTextPreference;

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
