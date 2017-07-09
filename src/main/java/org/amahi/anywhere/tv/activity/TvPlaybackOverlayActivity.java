package org.amahi.anywhere.tv.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.amahi.anywhere.R;

/**
 * Created by shasha on 9/7/17.
 */

public class TvPlaybackOverlayActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_playback_overlay);
    }
}
