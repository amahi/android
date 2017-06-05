package org.amahi.anywhere.amahitv.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.amahi.anywhere.amahitv.R;

public class IntroActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }
}
