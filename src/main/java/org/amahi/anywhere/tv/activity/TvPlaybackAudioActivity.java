package org.amahi.anywhere.tv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.view.KeyEvent;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.fragment.TvPlaybackAudioFragment;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;

import javax.inject.Inject;

import static org.amahi.anywhere.util.Fragments.Builder.buildAudioFragment;

public class TvPlaybackAudioActivity extends Activity {

    @Inject
    ServerClient serverClient;

    Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_audio_playback);
        setUpInjections();
        fragment = buildAudioFragment(getFile(), getShare(), getFiles());
        replaceFragment();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void replaceFragment() {
        getFragmentManager().beginTransaction().replace(R.id.playback_controls_fragment_container, fragment).commit();
    }

    private ServerShare getShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private ArrayList<ServerFile> getFiles() {
        return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                PlaybackControlsRow.PlayPauseAction playPauseAction = ((TvPlaybackAudioFragment) fragment).getmPlayPauseAction();
                ((TvPlaybackAudioFragment) fragment).togglePlayPause(playPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE);
                break;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                ((TvPlaybackAudioFragment) fragment).rewind();
                break;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                ((TvPlaybackAudioFragment) fragment).fastForward();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
