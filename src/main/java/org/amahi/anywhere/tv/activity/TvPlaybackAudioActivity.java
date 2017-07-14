package org.amahi.anywhere.tv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.fragment.TvPlaybackAudioFragment;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;

import javax.inject.Inject;

public class TvPlaybackAudioActivity extends Activity {

    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_audio_playback);
        setUpInjections();
        getFragmentManager().beginTransaction().replace(R.id.playback_controls_fragment_container, buildAudioFragment()).commit();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private Fragment buildAudioFragment(){
        Fragment fragment = new TvPlaybackAudioFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Intents.Extras.SERVER_SHARE,getShare());
        bundle.putParcelable(Intents.Extras.SERVER_FILE,getFile());
        bundle.putParcelableArrayList(Intents.Extras.SERVER_FILES,getFiles());
        fragment.setArguments(bundle);
        return fragment;
    }

    private ServerShare getShare(){
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private ServerFile getFile(){
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private ArrayList<ServerFile> getFiles(){
        return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
    }
}
