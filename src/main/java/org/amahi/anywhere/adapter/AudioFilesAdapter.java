package org.amahi.anywhere.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.l4digital.fastscroll.FastScroller;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.model.AudioFile;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.AudioFileViewHolder> implements FastScroller.SectionIndexer {

    private final Context context;
    private ServerClient serverClient;
    private List<ServerFile> serverFiles;
    private List<AudioFile> audioFiles;
    private RecyclerViewItemClickListener itemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ServerShare serverShare;

    public AudioFilesAdapter(@NonNull ServerClient serverClient,
                             @NonNull List<ServerFile> serverFiles,
                             @NonNull ServerShare serverShare,
                             @NonNull Context context,
                             @NonNull RecyclerViewItemClickListener itemClickListener) {
        this.serverClient = serverClient;
        this.serverFiles = serverFiles;
        this.serverShare = serverShare;
        this.context = context;
        this.itemClickListener = itemClickListener;

        prepareAudioFiles();

        BusProvider.getBus().register(this);
    }

    private void prepareAudioFiles() {
        audioFiles = new ArrayList<>();
        for (int i = 0; i < serverFiles.size(); i++) {
            AudioFile audioFile = new AudioFile();
            audioFiles.add(audioFile);
        }
    }

    @Override
    public CharSequence getSectionText(int selectedPosition) {
        return audioFiles.get(selectedPosition).getTitle().subSequence(0, 1);
    }

    @Override
    public AudioFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioFileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_audio_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AudioFileViewHolder holder, int position) {
        ServerFile file = serverFiles.get(position);
        AudioFile audioFile = audioFiles.get(position);

        if (audioFile.getTitle() == null) {
            holder.audioName.setText(file.getName());

            AudioMetadataRetrievingTask.newInstance(context, getAudioUri(file), file)
                .setAudioFileHolder(holder)
                .execute();
        } else {
            holder.audioName.setText(audioFile.getTitle());
            holder.audioSubtitle.setText(audioFile.getSubtitle());
        }

        if (selectedPosition == holder.getAdapterPosition()) {
            holder.audioName.setTextColor(ContextCompat.getColor(context, R.color.accent));
            holder.audioSubtitle.setTextColor(ContextCompat.getColor(context, R.color.accent));
        } else {
            holder.audioName.setTextColor(context.getResources().getColor(R.color.primary_text));
            holder.audioSubtitle.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(holder.getAdapterPosition());
            itemClickListener.onItemClick(v, holder.getAdapterPosition());
        });
    }

    private Uri getAudioUri(ServerFile file) {
        if (isFileAvailableOffline(file)) {
            return null;
        }

        if (serverShare != null) {
            return serverClient.getFileUri(serverShare, file);
        } else {
            RecentFileRepository repository = new RecentFileRepository(context);
            RecentFile recentFile = repository.getRecentFile(file.getUniqueKey());
            if (recentFile != null) {
                return Uri.parse(recentFile.getUri());
            }
            return null;
        }
    }

    private boolean isFileAvailableOffline(ServerFile file) {
        OfflineFileRepository repository = new OfflineFileRepository(context);
        OfflineFile offlineFile = repository.getOfflineFile(file.getName(), file.getModificationTime().getTime());
        return offlineFile != null && offlineFile.getState() == OfflineFile.DOWNLOADED;
    }

    @Override
    public int getItemCount() {
        return audioFiles.size();
    }

    public void setSelectedPosition(int position) {
        notifyItemChanged(selectedPosition);
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        AudioMetadata audioMetadata = event.getAudioMetadata();
        if (event.getAudioFileHolder() != null && audioMetadata != null) {
            AudioFile audioFile = audioFiles.get(event.getAudioFileHolder().getAdapterPosition());
            audioFile.setTitle(audioMetadata.getAudioTitle());
            if (audioMetadata.getAudioArtist() != null) {
                audioFile.setSubtitle(audioMetadata.getAudioArtist());
            } else {
                audioFile.setSubtitle(context.getString(R.string.unknown_artist));
            }
            notifyItemChanged(event.getAudioFileHolder().getAdapterPosition());
        }
    }

    public void tearDownCallbacks() {
        BusProvider.getBus().unregister(this);
    }

    public static class AudioFileViewHolder extends RecyclerView.ViewHolder {
        TextView audioName, audioSubtitle;

        public AudioFileViewHolder(View itemView) {
            super(itemView);

            audioName = itemView.findViewById(R.id.audio_name_text);
            audioSubtitle = itemView.findViewById(R.id.audio_subtitle_text);
        }
    }
}
