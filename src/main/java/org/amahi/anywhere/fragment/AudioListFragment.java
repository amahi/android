package org.amahi.anywhere.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.AudioFilesAdapter;
import org.amahi.anywhere.bus.AudioControlChangeEvent;
import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.RecyclerViewItemClickListener;

import java.util.List;

import javax.inject.Inject;

public class AudioListFragment extends Fragment implements RecyclerViewItemClickListener {

    public static final String TAG = "audio_list_fragment";

    @Inject
    public ServerClient serverClient;

    private RecyclerView recyclerView;
    private AudioFilesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpInjections();
        recyclerView = view.findViewById(R.id.audio_list_r_view);
        setUpAudioList();

        BusProvider.getBus().register(this);
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpAudioList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AudioFilesAdapter(serverClient,
            getFiles(), getShare(), getContext(), this);
        adapter.setSelectedPosition(getFiles().indexOf(getAudioFile()));

        recyclerView.setAdapter(adapter);
    }

    private ServerFile getAudioFile() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
    }

    private List<ServerFile> getFiles() {
        return getArguments().getParcelableArrayList(Fragments.Arguments.SERVER_FILES);
    }

    private ServerShare getShare() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (getFiles().indexOf(getAudioFile()) != position) {
            changeAudio(position);
        }
    }

    private void saveAudioFile(ServerFile serverFile) {
        getArguments().putParcelable(Fragments.Arguments.SERVER_FILE, serverFile);
    }

    private void changeAudio(int position) {
        BusProvider.getBus().post(new AudioControlChangeEvent(position));
    }

    @Subscribe
    public void onAudioControlChange(AudioControlChangeEvent event) {
        saveAudioFile(getFiles().get(event.getPosition()));
        adapter.setSelectedPosition(event.getPosition());
    }

    @Subscribe
    public void onAudioControlNext(AudioControlNextEvent event) {
        int currentPosition = getFiles().indexOf(getAudioFile());
        currentPosition += 1;
        currentPosition %= getFiles().size();
        saveAudioFile(getFiles().get(currentPosition));
        adapter.setSelectedPosition(currentPosition);
    }

    @Subscribe
    public void onAudioControlPrevious(AudioControlPreviousEvent event) {
        int currentPosition = getFiles().indexOf(getAudioFile());
        currentPosition += (getFiles().size() - 1);
        currentPosition %= getFiles().size();
        saveAudioFile(getFiles().get(currentPosition));
        adapter.setSelectedPosition(currentPosition);
    }

    @Override
    public boolean onLongItemClick(View view, int position) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        AudioFilesAdapter adapter = (AudioFilesAdapter) recyclerView.getAdapter();
        adapter.tearDownCallbacks();
        BusProvider.getBus().unregister(this);
    }
}
