package org.amahi.anywhere.tv.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.text.format.Formatter;

import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.server.model.ServerFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    private Context mContext;

    public AudioDetailsDescriptionPresenter(Context context){
        this.mContext = context;
    }

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        AudioMetadataRetrievedEvent event = (AudioMetadataRetrievedEvent)item;

        if(event.getAudioTitle()!=null)
            viewHolder.getTitle().setText(event.getAudioTitle());
        else
            viewHolder.getTitle().setText(event.getServerFile().getName());

        if(event.getAudioAlbum()!=null && event.getAudioArtist()!=null){
            viewHolder.getSubtitle().setText(event.getAudioAlbum()+" - "+event.getAudioArtist());
        }
        else
            viewHolder.getSubtitle().setText(getDate(event.getServerFile()));

        viewHolder.getBody().setText(getSize(event.getServerFile()));
    }

    private String getDate(ServerFile serverFile) {
        Date d = serverFile.getModificationTime();
        SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy");
        return dt.format(d);
    }

    private String getSize(ServerFile serverFile) {
        return Formatter.formatFileSize(mContext, serverFile.getSize());
    }
}
