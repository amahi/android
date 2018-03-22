package org.amahi.anywhere.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.Fragments;

import javax.inject.Inject;

/**
 * Audio fragment. Shows a single audio cover image.
 */

public class ServerFileAudioFragment extends Fragment {
    @Inject
    ServerClient serverClient;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.fragment_server_file_audio, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        setUpAudioMetadata();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpAudioMetadata() {
        AudioMetadataRetrievingTask
            .newInstance(getActivity(), getAudioUri(), getFile())
            .execute();
    }

    private Uri getAudioUri() {
        return serverClient.getFileUri(getShare(), getFile());
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        ServerFile audioFile = getFile();
        if (audioFile != null && audioFile == event.getServerFile()) {
            AudioMetadata metadata = event.getAudioMetadata();
            setupImage(metadata.getAudioAlbumArt());
        }
    }

    private void setupImage(Bitmap audioAlbumArt) {
        if (audioAlbumArt == null) {
            audioAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_audiotrack);
            getImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        getImageView().setImageBitmap(audioAlbumArt);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    private ServerShare getShare() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
    }

    public ImageView getImageView() {
        return (ImageView) getView().findViewById(R.id.image_album_art);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownImageContent();
    }

    private void tearDownImageContent() {
        getImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        getImageView().setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_audiotrack));
    }

}
