/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerFilesImagePagerAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileCopiedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.fragment.PrepareDialogFragment;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.FullScreenHelper;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Preferences;
import org.amahi.anywhere.view.ClickableViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Image activity. Shows images as a slide show.
 * Backed up by {@link org.amahi.anywhere.view.TouchImageView}.
 */
public class ServerFileImageActivity extends AppCompatActivity implements
    ViewPager.OnPageChangeListener,
    SessionManagerListener<CastSession> {
    private static final Set<String> SUPPORTED_FORMATS;

    static {
        SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
            "image/bmp",
            "image/jpeg",
            "image/gif",
            "image/png",
            "image/webp"
        ));
    }

    @Inject
    ServerClient serverClient;
    private CastSession mCastSession;
    private CastContext mCastContext;
    private int imagePosition;

    public static boolean supports(String mime_type) {
        return SUPPORTED_FORMATS.contains(mime_type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_file_image);

        setUpInjections();

        setUpHomeNavigation();

        prepareFiles();

        setUpImage();

        setUpRecentFiles(getFile());

        setUpCast();

        setUpFullScreen();
    }

    private void prepareFiles() {
        int fileType = getIntent().getIntExtra(Intents.Extras.FILE_TYPE, FileManager.SERVER_FILE);
        if (fileType == FileManager.RECENT_FILE) {
            RecentFileRepository repository = new RecentFileRepository(this);
            RecentFile recentFile = repository.getRecentFile(getIntent().getStringExtra(Intents.Extras.UNIQUE_KEY));
            ServerFile serverFile = new ServerFile(recentFile.getName(), recentFile.getModificationTime(), recentFile.getMime());
            serverFile.setSize(recentFile.getSize());
            serverFile.setMime(recentFile.getMime());
            getIntent().putExtra(Intents.Extras.SERVER_FILE, serverFile);
            List<ServerFile> serverFiles = new ArrayList<>();
            serverFiles.add(serverFile);
            getIntent().putExtra(Intents.Extras.SERVER_FILES, new ArrayList<Parcelable>(serverFiles));
        }
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpFullScreen() {
        final FullScreenHelper fullScreen = new FullScreenHelper(getSupportActionBar(), getImagePager());
        fullScreen.enableOnClickToggle(false);
        getImagePager().setOnViewPagerClickListener(viewPager -> fullScreen.toggle());
        fullScreen.init();
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpImage() {
        setUpImageTitle();
        setUpImageAdapter();
        setUpImageListener();
        setUpImagePosition();
    }

    private boolean isCastConnected() {
        return mCastSession != null && mCastSession.isConnected();
    }

    private void setUpCast() {
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        if (isCastConnected()) {
            loadRemoteMedia();
        }
    }

    private void setUpImageTitle() {
        setUpImageTitle(getFile());
    }

    private void setUpImageTitle(ServerFile file) {
        getSupportActionBar().setTitle(file.getName());
    }

    private ServerFile getFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private void setUpImageAdapter() {
        getImagePager().setAdapter(new ServerFilesImagePagerAdapter(getSupportFragmentManager(), getShare(), getImageFiles()));
    }

    private ClickableViewPager getImagePager() {
        return findViewById(R.id.pager_images);
    }

    private ServerShare getShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private List<ServerFile> getImageFiles() {
        List<ServerFile> imageFiles = new ArrayList<>();

        for (ServerFile file : getFiles()) {
            if (SUPPORTED_FORMATS.contains(file.getMime())) {
                imageFiles.add(file);
            }
        }

        return imageFiles;
    }

    private List<ServerFile> getFiles() {
        return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
    }

    private void setUpImagePosition() {
        imagePosition = getImageFiles().indexOf(getFile());
        getImagePager().setCurrentItem(imagePosition);
    }

    private void setUpImageListener() {
        getImagePager().addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageSelected(int position) {
        this.imagePosition = position;
        setUpImageTitle(getImageFiles().get(position));
        if (isCastConnected()) {
            loadRemoteMedia();
        }

        setUpRecentFiles(getFiles().get(position));
    }

    private void setUpRecentFiles(ServerFile serverFile) {
        long size;
        if (isFileAvailableOffline(serverFile)) {
            size = new File(getOfflineFileUri(serverFile.getName())).length();
        } else {
            size = serverFile.getSize();
        }

        String serverName = Preferences.getServerName(this);

        RecentFile recentFile = new RecentFile(serverFile.getUniqueKey(),
            getImageUri(serverFile),
            serverName,
            System.currentTimeMillis(),
            size);
        RecentFileRepository recentFileRepository = new RecentFileRepository(this);
        recentFileRepository.insert(recentFile);
    }

    private boolean isFileAvailableOffline(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null && file.getState() == OfflineFile.DOWNLOADED;
    }

    private String getUriFrom(String name, Date modificationTime) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile offlineFile = repository.getOfflineFile(name, modificationTime.getTime());
        return offlineFile.getFileUri();
    }

    private String getOfflineFileUri(String name) {
        return (getFilesDir() + "/" + Downloader.OFFLINE_PATH + "/" + name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_server_file_image, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
            R.id.media_route_menu_item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_share:
                prepareDownload();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void startFileSharingActivity() {
        startFileDownloading(getShare(), getCurrentFile());
    }

    private void prepareDownload() {

        ServerFile serverFile = getCurrentFile();

        if (isFileAvailableOffline(serverFile)) {
            prepareDownloadingFile(serverFile);
        } else {
            startFileDownloading(getShare(), getCurrentFile());
        }
    }

    private void prepareDownloadingFile(ServerFile file) {
        PrepareDialogFragment fragment = new PrepareDialogFragment();
        fragment.show(getSupportFragmentManager(), "prepare_dialog");
        FileManager fm = FileManager.newInstance(this);
        Uri offlinePath = fm.getContentUriForOfflineFile(file.getName());
        File sourceLocation = new File(offlinePath.toString());
        File downloadLocation = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file.getName());

        fm.copyFile(sourceLocation, downloadLocation);
    }

    private ServerFile getCurrentFile() {
        return getImageFiles().get(getImagePager().getCurrentItem());
    }

    private void startFileDownloading(ServerShare share, ServerFile file) {
        showFileDownloadingFragment(share, file);
    }

    private void showFileDownloadingFragment(ServerShare share, ServerFile file) {
        DialogFragment fragment = ServerFileDownloadingFragment.newInstance(share, file, FileOption.SHARE);
        fragment.show(getFragmentManager(), ServerFileDownloadingFragment.TAG);
    }

    @Subscribe
    public void onFileDownloaded(FileDownloadedEvent event) {
        finishFileDownloading(event.getFileUri());
    }

    private void finishFileDownloading(Uri fileUri) {
        startFileSharingActivity(getCurrentFile(), fileUri);
    }

    private void startFileSharingActivity(ServerFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileSharingIntent(file, fileUri);
        startActivity(intent);
    }

    @Subscribe
    public void onFileCopied(FileCopiedEvent event) {
        Uri contentUri = FileManager.newInstance(this).getContentUri(event.getTargetLocation());

        dismissPreparingDialog();
        finishFileDownloading(contentUri);
    }

    private void dismissPreparingDialog() {
        PrepareDialogFragment fragment = (PrepareDialogFragment) getSupportFragmentManager().findFragmentByTag("prepare_dialog");
        if (fragment != null && fragment.isAdded()) {
            fragment.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCastContext.getSessionManager().addSessionManagerListener(this, CastSession.class);
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCastContext.getSessionManager().removeSessionManagerListener(this, CastSession.class);
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
        onApplicationDisconnected();
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
        onApplicationDisconnected();
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
        onApplicationDisconnected();
    }

    @Override
    public void onSessionStarting(CastSession session) {
    }

    @Override
    public void onSessionEnding(CastSession session) {
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {
    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {
    }

    private void onApplicationConnected(CastSession castSession) {
        mCastSession = castSession;
        invalidateOptionsMenu();
        loadRemoteMedia();
    }

    private void onApplicationDisconnected() {
        mCastSession = null;
        invalidateOptionsMenu();
    }

    private void loadRemoteMedia() {
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient != null) {

            MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().build();
            remoteMediaClient.load(buildMediaInfo(), mediaLoadOptions);
        }
    }

    private MediaInfo buildMediaInfo() {
        ServerFile file = getImageFiles().get(imagePosition);
        MediaMetadata imageMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        imageMetadata.putString(MediaMetadata.KEY_TITLE, file.getNameOnly());
        imageMetadata.putString(MediaMetadata.KEY_ARTIST, "");
        imageMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, "");
        String imageUrl = getImageUri(getCurrentFile());
        imageMetadata.addImage(new WebImage(Uri.parse(imageUrl)));
        imageMetadata.addImage(new WebImage(Uri.parse(imageUrl)));
        return new MediaInfo.Builder(imageUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(file.getMime())
            .setMetadata(imageMetadata)
            .build();
    }

    private String getImageUri(ServerFile serverFile) {
        int fileType = getIntent().getIntExtra(Intents.Extras.FILE_TYPE, FileManager.SERVER_FILE);

        if (fileType == FileManager.RECENT_FILE) {
            return getRecentFileUri();
        }
        if (getShare() == null) {
            OfflineFileRepository repository = new OfflineFileRepository(this);
            OfflineFile offlineFile = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
            if (offlineFile != null) {
                return offlineFile.getFileUri();
            }
        }
        return serverClient.getFileUri(getShare(), getCurrentFile()).toString();
    }

    private String getRecentFileUri() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return repository.getRecentFile(getCurrentFile().getUniqueKey()).getUri();
    }
}
