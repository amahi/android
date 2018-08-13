package org.amahi.anywhere.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioControlPlayPauseEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.AudioStopEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.util.AudioMetadataFormatter;

public class AudioControllerFragment extends Fragment {

    private AudioService audioService;
    private TextView audioTitleText, audioSubtitleText;
    private ImageView audioAlbumArt;
    private ImageButton playPauseButton;
    private ProgressBar progressBar;
    private boolean isPlaying = false;
    private Runnable progressTask = () -> {
        if (audioService != null) {
            long currentPosition = audioService.getAudioPlayer().getCurrentPosition();
            long duration = audioService.getAudioPlayer().getDuration();
            long progress = duration != 0 ? (100 * currentPosition) / duration : 0;
            int buffering = audioService.getAudioPlayer().getBufferedPercentage();
            UpdateProgressBar((int) progress, buffering);
            UpdatePlayPause(audioService.getAudioPlayer().getPlayWhenReady());
            new Handler().postDelayed(this.progressTask, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_controller, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        audioAlbumArt = view.findViewById(R.id.art_image_view);
        audioTitleText = view.findViewById(R.id.audio_title_text);
        audioSubtitleText = view.findViewById(R.id.audio_subtitle_text);
        ImageButton closeButton = view.findViewById(R.id.stop_audio_btn);
        playPauseButton = view.findViewById(R.id.play_pause_btn);
        progressBar = view.findViewById(R.id.media_progress_bar);
        RelativeLayout controller = view.findViewById(R.id.audio_controller);

        playPauseButton.setOnClickListener(v -> {
            if (audioService.getAudioPlayer().getPlayWhenReady()) {
                playPauseButton.setImageResource(R.drawable.lb_ic_play);
            } else {
                playPauseButton.setImageResource(R.drawable.lb_ic_pause);
            }
            BusProvider.getBus().post(new AudioControlPlayPauseEvent());
        });

        closeButton.setOnClickListener(v -> BusProvider.getBus().post(new AudioStopEvent()));

        controller.setOnClickListener(v -> BusProvider.getBus().post(
            new FileOpeningEvent(audioService.getAudioShare(),
                audioService.getAudioFiles(),
                audioService.getAudioFile())));
    }

    public void connect(AudioService audioService) {
        this.audioService = audioService;

        if (audioService.getAudioPlayer().getPlayWhenReady()) {
            playPauseButton.setImageResource(R.drawable.lb_ic_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.lb_ic_play);
        }

        AudioMetadataFormatter formatter = audioService.getAudioMetadataFormatter();
        if (formatter != null) {
            setUpAudioMetadata(formatter.getAudioTitle(audioService.getAudioFile()),
                formatter.getAudioSubtitle(audioService.getAudioShare()), audioService.getAudioAlbumArt());
        } else {
            audioTitleText.setText(audioService.getAudioFile().getName());
        }

        new Handler().postDelayed(progressTask, 1000);
    }

    private void setUpAudioMetadata(String audioTitle, String audioSubtitle, Bitmap albumArt) {
        audioTitleText.setText(audioTitle);
        audioSubtitleText.setText(audioSubtitle);
        if (albumArt != null) {
            audioAlbumArt.setImageBitmap(albumArt);
            audioAlbumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            audioAlbumArt.setImageResource(R.drawable.default_audiotrack);
            audioAlbumArt.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        if (audioService != null) {
            ServerFile audioFile = audioService.getAudioFile();
            if (audioFile != null && event.getServerFile() != null && audioFile.getUniqueKey().equals(event.getServerFile().getUniqueKey())) {
                AudioMetadata metadata = event.getAudioMetadata();
                if (metadata != null) {
                    setUpAudioMetadata(metadata.getAudioTitle(), metadata.getAudioArtist(), metadata.getAudioAlbumArt());
                }
            }
        }
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

    private void UpdatePlayPause(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.lb_ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.lb_ic_play);
            }
        }
    }

    private void UpdateProgressBar(int progress, int buffering) {
        progressBar.setProgress(progress);
        progressBar.setSecondaryProgress(buffering);
    }
}
