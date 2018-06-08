package org.amahi.anywhere.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.RecentFilesViewHolder> {

    private Context context;
    private ServerFileClickListener mListener;
    private List<RecentFile> recentFiles;

    public RecentFilesAdapter(Context context, List<RecentFile> recentFiles) {
        this.context = context;
        this.recentFiles = recentFiles;
        this.mListener = (ServerFileClickListener) context;
        BusProvider.getBus().register(this);
    }

    @Override
    public RecentFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentFilesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_server_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecentFilesViewHolder holder, int position) {

        RecentFile file = recentFiles.get(position);

        setUpViewHolder(file, holder);
    }

    private void setUpViewHolder(RecentFile file, RecentFilesViewHolder fileHolder) {
        Uri uri = Uri.parse(file.getUri());
        String name = Uri.parse(uri.getQueryParameter("p")).getLastPathSegment();
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(name.lastIndexOf(".") + 1));
        String size = Formatter.formatFileSize(context, file.getSize());

        Date d = new Date(file.getVisitTime());
        SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy", Locale.getDefault());
        String date = dt.format(d);

        fileHolder.fileTextView.setText(name);
        fileHolder.fileSize.setText(size);
        fileHolder.fileLastVisited.setText(date);

        if (Mimes.match(mime) == Mimes.Type.IMAGE) {
            setImageIcon(file, fileHolder.fileIconView);
        } else if (Mimes.match(mime) == Mimes.Type.AUDIO) {
            setUpAudioArt(file, fileHolder.fileIconView);
        } else {
            fileHolder.fileIconView.setImageResource(R.drawable.ic_file_video);
        }

        setUpViewHolderListeners(fileHolder);
    }

    private void setUpViewHolderListeners(RecentFilesViewHolder fileHolder) {
        fileHolder.itemView.setOnClickListener(view -> mListener.onItemClick(fileHolder.itemView, fileHolder.getAdapterPosition()));

        fileHolder.moreOptions.setOnClickListener(view -> {
            mListener.onMoreOptionClick(fileHolder.itemView, fileHolder.getAdapterPosition());
        });
    }

    private void setImageIcon(RecentFile file, ImageView fileIconView) {
        Glide.with(fileIconView.getContext())
            .load(Uri.parse(file.getUri()))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.ic_file_image)
            .into(fileIconView);
    }

    private void setUpAudioArt(RecentFile file, ImageView fileIconView) {
        new AudioMetadataRetrievingTask(context, Uri.parse(file.getUri()), file.getUniqueKey())
            .setImageView(fileIconView)
            .execute();
    }

    public void replaceWith(List<RecentFile> files) {
        this.recentFiles = files;

        notifyDataSetChanged();
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        ImageView imageView = event.getImageView();
        Bitmap bitmap = event.getAudioMetadata().getAudioAlbumArt();
        if (bitmap != null && imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void tearDownCallbacks() {
        BusProvider.getBus().unregister(this);
    }

    @Override
    public int getItemCount() {
        return recentFiles.size();
    }

    public void removeFile(int selectedPosition) {
        recentFiles.remove(selectedPosition);

        notifyDataSetChanged();
    }

    class RecentFilesViewHolder extends RecyclerView.ViewHolder {

        ImageView fileIconView, moreOptions;
        TextView fileTextView, fileSize, fileLastVisited;
        LinearLayout moreInfo;

        RecentFilesViewHolder(View itemView) {
            super(itemView);
            fileIconView = itemView.findViewById(R.id.icon);
            fileTextView = itemView.findViewById(R.id.text);
            fileSize = itemView.findViewById(R.id.file_size);
            fileLastVisited = itemView.findViewById(R.id.last_modified);
            moreInfo = itemView.findViewById(R.id.more_info);
            moreOptions = itemView.findViewById(R.id.more_options);
        }
    }
}
