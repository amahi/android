package org.amahi.anywhere.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileMetadataRetrievedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.task.FileMetadataRetrievingTask;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.util.Collections;

/**
 * Files adapter. Visualizes files
 * for the {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */

public class ServerFilesMetadataAdapter extends FilesFilterAdapter {

    public ServerFilesMetadataAdapter(Context context, ServerClient serverClient) {
        this.layoutInflater = LayoutInflater.from(context);

        this.serverClient = serverClient;

        this.files = Collections.emptyList();
        this.filteredFiles = Collections.emptyList();

        BusProvider.getBus().register(this);
    }

    public void setOnClickListener(ServerFileClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View fileView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_server_file_metadata_item, parent, false);

        fileView.setTag(Tags.FILE_TITLE, fileView.findViewById(R.id.text));
        fileView.setTag(Tags.FILE_ICON, fileView.findViewById(R.id.icon));

        return new ServerFileMetadataViewHolder(fileView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServerFile file = getItems().get(position);
        final ServerFileMetadataViewHolder fileHolder = (ServerFileMetadataViewHolder) holder;
        unbindFileView(file, fileHolder);

        if (Mimes.match(file.getMime()) != Mimes.Type.VIDEO) {
            bindFileView(file, fileHolder);
        } else {
            if (!file.isMetaDataFetched()) {
                bindFileMetadataView(file, fileHolder.itemView);
            } else {
                bindView(file, file.getFileMetadata(), fileHolder);
            }
        }
        if (Mimes.match(file.getMime()) == Mimes.Type.IMAGE) {
            setUpImageIcon(file, fileHolder.fileIcon);
        }
        if (Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY) {
            fileHolder.moreOptions.setVisibility(View.GONE);
        } else {
            fileHolder.moreOptions.setVisibility(View.VISIBLE);
        }

        fileHolder.itemView.setOnClickListener(view -> {
            mListener.onItemClick(fileHolder.itemView, fileHolder.getAdapterPosition());
        });

        fileHolder.moreOptions.setOnClickListener(view -> {
            selectedPosition = fileHolder.getAdapterPosition();
            mListener.onMoreOptionClick(fileHolder.itemView, fileHolder.getAdapterPosition());
        });
    }

    private void unbindFileView(ServerFile file, ServerFileMetadataViewHolder holder) {
        holder.fileTitle.setText(null);
        holder.fileTitle.setBackgroundResource(android.R.color.transparent);

        holder.fileIcon.setImageResource(Mimes.getFileIcon(file));
        holder.fileIcon.setBackgroundResource(R.color.background_secondary);
    }

    private void bindFileView(ServerFile file, ServerFileMetadataViewHolder holder) {
        SpannableStringBuilder sb = new SpannableStringBuilder(file.getName());
        if (queryString != null && !TextUtils.isEmpty(queryString)) {
            int searchMatchPosition = file.getName().toLowerCase().indexOf(queryString.toLowerCase());
            if (searchMatchPosition != -1)
                sb.setSpan(fcs, searchMatchPosition, searchMatchPosition + queryString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        holder.fileTitle.setText(sb);
        holder.fileTitle.setBackgroundResource(R.color.background_transparent_secondary);

        holder.fileIcon.setImageResource(Mimes.getFileIcon(file));
        holder.fileIcon.setBackgroundResource(R.color.background_secondary);
    }

    private void bindFileMetadataView(ServerFile file, View fileView) {
        fileView.setTag(Tags.SHARE, serverShare);
        fileView.setTag(Tags.FILE, file);

        new FileMetadataRetrievingTask(serverClient, fileView).execute();
    }

    @Subscribe
    public void onFileMetadataRetrieved(FileMetadataRetrievedEvent event) {
        event.getFile().setMetaDataFetched(true);
        bindView(event.getFile(), event.getFileMetadata(), new ServerFileMetadataViewHolder(event.getFileView()));
    }

    private void bindView(ServerFile file, ServerFileMetadata fileMetadata, final ServerFileMetadataViewHolder holder) {
        if (fileMetadata == null) {
            bindFileView(file, holder);
        } else {
            file.setFileMetadata(fileMetadata);
            bindFileMetadataView(file, fileMetadata, holder);
        }
    }

    private void bindFileMetadataView(ServerFile file, ServerFileMetadata fileMetadata, ServerFileMetadataViewHolder holder) {
        holder.fileTitle.setText(null);
        holder.fileTitle.setBackgroundResource(android.R.color.transparent);

        Glide.with(holder.itemView.getContext())
            .load(fileMetadata.getArtworkUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .fitCenter()
            .placeholder(Mimes.getFileIcon(file))
            .error(Mimes.getFileIcon(file))
            .into(holder.fileIcon);
    }

    public void tearDownCallbacks() {
        BusProvider.getBus().unregister(this);
    }

    public static final class Tags {
        public static final int SHARE = R.id.container_files;
        public static final int FILE = R.attr.server_share;
        public static final int FILE_TITLE = R.id.text;
        public static final int FILE_ICON = R.id.icon;

        private Tags() {
        }
    }

    public class ServerFileMetadataViewHolder extends RecyclerView.ViewHolder {

        ImageView fileIcon, moreOptions;
        TextView fileTitle;

        ServerFileMetadataViewHolder(View itemView) {
            super(itemView);
            fileIcon = (ImageView) itemView.getTag(Tags.FILE_ICON);
            fileTitle = (TextView) itemView.getTag(Tags.FILE_TITLE);
            moreOptions = itemView.findViewById(R.id.more_options);
        }
    }
}
