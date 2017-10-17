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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerFilesImagePagerAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.FullScreenHelper;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.view.ClickableViewPager;

import java.util.ArrayList;
import java.util.Arrays;
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

        setUpImage();

        setUpCast();

        setUpFullScreen();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpFullScreen() {
        final FullScreenHelper fullScreen = new FullScreenHelper(getSupportActionBar(), getImagePager());
        fullScreen.enableOnClickToggle(false);
        getImagePager().setOnViewPagerClickListener(new ClickableViewPager.OnClickListener() {
            @Override
            public void onViewPagerClick(ViewPager viewPager) {
                fullScreen.toggle();
            }
        });
        fullScreen.init();
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpImage() {
        setUpImageTitle();
        setUpImageAdapter();
        setUpImagePosition();
        setUpImageListener();
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
        return (ClickableViewPager) findViewById(R.id.pager_images);
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
                startFileSharingActivity();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void startFileSharingActivity() {
        startFileDownloading(getShare(), getCurrentFile());
    }

    private ServerFile getCurrentFile() {
        return getImageFiles().get(getImagePager().getCurrentItem());
    }

    private void startFileDownloading(ServerShare share, ServerFile file) {
        showFileDownloadingFragment(share, file);
    }

    private void showFileDownloadingFragment(ServerShare share, ServerFile file) {
        DialogFragment fragment = ServerFileDownloadingFragment.newInstance(share, file);
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
            remoteMediaClient.load(buildMediaInfo());
        }
    }

    private MediaInfo buildMediaInfo() {
        ServerFile file = getImageFiles().get(imagePosition);
        MediaMetadata imageMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        imageMetadata.putString(MediaMetadata.KEY_TITLE, file.getNameOnly());
        String imageUrl = serverClient.getFileUri(getShare(), file).toString();
        return new MediaInfo.Builder(imageUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(file.getMime())
            .setMetadata(imageMetadata)
            .build();
    }
}
