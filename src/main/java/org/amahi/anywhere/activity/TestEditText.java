package org.amahi.anywhere.activity;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

public class TestEditText extends TextInputEditText {

    private static ImageView imageView;
    private static EditText user, password;

    public static void setImageView(ImageView view, EditText username, EditText pass) {
        imageView = view;
        user = username;
        password = pass;
    }

    public TestEditText(Context context) {
        super(context);
    }

    public TestEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme (int keyCode, KeyEvent event) {
        user.clearFocus();
        password.clearFocus();

        imageView.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        
        return true;
    }
}
