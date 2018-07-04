package org.amahi.anywhere.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileCopiedEvent;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.OfflineCanceledEvent;
import org.amahi.anywhere.bus.OfflineFileDeleteEvent;
import org.amahi.anywhere.bus.ServerFileDeleteEvent;
import org.amahi.anywhere.bus.ServerFileDownloadingEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.fragment.PrepareDialogFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class OfflineFilesActivity extends AppCompatActivity {

    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_files);

        setUpInjections();

        setUpHomeNavigation();

        setUpFiles(savedInstanceState);
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpFiles(Bundle state) {
        setUpFilesTitle();
        setUpFilesFragment();
    }

    private void setUpFilesTitle() {
        getSupportActionBar().setTitle(R.string.title_offline_files);
    }

    private void setUpFilesFragment() {
        Fragments.Operator.at(this).set(buildFilesFragment(), R.id.container_files);
    }

    private Fragment buildFilesFragment() {
        return Fragments.Builder.buildServerFilesFragmentForOfflineFiles();
    }

    @Subscribe
    public void onFileOpening(FileOpeningEvent event) {
        if (isFileDownloaded(event.getFile())) {
            startFileActivity(event.getFile(), event.getFiles());
        } else {
            showDownloadingMessage();
        }
    }

    private boolean isFileDownloaded(ServerFile file) {
        OfflineFileRepository offlineFileRepository = new OfflineFileRepository(this);
        OfflineFile offlineFile = offlineFileRepository.getOfflineFile(file.getName(), file.getModificationTime().getTime());
        return offlineFile.getState() == OfflineFile.DOWNLOADED;
    }

    private void startFileActivity(ServerFile file, List<ServerFile> serverFiles) {
        Intent intent = Intents.Builder.with(this).buildServerFileIntent(null, serverFiles, file);
        startActivity(intent);
    }

    @Subscribe
    public void onFileSharing(ServerFileSharingEvent event) {
        if (isFileDownloaded(event.getFile())) {
            Uri contentUri = getContentUri(event.getFile());
            startFileSharingActivity(event.getFile(), contentUri);
        } else {
            Snackbar.make(getParentView(), R.string.message_progress_file_downloading, Snackbar.LENGTH_LONG).show();
        }
    }

    private Uri getContentUri(ServerFile serverFile) {
        return FileManager.newInstance(this).getContentUriForOfflineFile(serverFile.getName());
    }

    private void startFileSharingActivity(ServerFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileSharingIntent(file, fileUri);
        startActivity(intent);
    }

    @Subscribe
    public void onFileDownloading(ServerFileDownloadingEvent event) {
        if (isFileDownloaded(event.getFile())) {
            startFileDownloading(event.getFile());
        } else {
            showDownloadingMessage();
        }
    }

    private void startFileDownloading(ServerFile file) {
        showPreparingDialog();
        File offlineLocation = getOfflineFileLocation(file.getName());
        File downloadLocation = getDownloadFileLocation(file.getName());
        FileManager.newInstance(this).copyFile(offlineLocation, downloadLocation);
    }

    private void showPreparingDialog() {
        PrepareDialogFragment fragment = new PrepareDialogFragment();
        fragment.show(getSupportFragmentManager(), "prepare_dialog");
    }

    private File getOfflineFileLocation(String name) {
        return new File(getFilesDir(), Downloader.OFFLINE_PATH + "/" + name);
    }

    private File getDownloadFileLocation(String name) {
        return new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS),
            name);
    }

    private void showDownloadingMessage() {
        Snackbar.make(getParentView(), R.string.message_progress_file_downloading, Snackbar.LENGTH_LONG).show();
    }

    @Subscribe
    public void onFileCopied(FileCopiedEvent event) {
        dismissPreparingDialog();

        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (event.getTargetLocation().toString().contains(downloadDirectory.toString())) {
            Snackbar.make(getParentView(), R.string.message_file_download_complete, Snackbar.LENGTH_LONG)
                .show();
        }
    }

    private void dismissPreparingDialog() {
        PrepareDialogFragment fragment = (PrepareDialogFragment) getSupportFragmentManager().findFragmentByTag("prepare_dialog");
        if (fragment != null && fragment.isAdded()) {
            fragment.dismiss();
        }
    }

    private View getParentView() {
        return findViewById(R.id.parent_view);
    }

    @Subscribe
    public void onFileDeleting(OfflineFileDeleteEvent event) {
        deleteFileFromOfflineStorage(event.getFile());
    }

    private void deleteFileFromOfflineStorage(ServerFile serverFile) {
        OfflineFileRepository offlineFileRepository = new OfflineFileRepository(this);
        OfflineFile offlineFile = offlineFileRepository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        if (offlineFile != null) {
            if (offlineFile.getState() == OfflineFile.DOWNLOADING) {
                stopDownloading(offlineFile.getDownloadId());
            }

            File file = getOfflineFileLocation(offlineFile.getName());
            FileManager.newInstance(this).deleteFile(file);

            offlineFileRepository.delete(offlineFile);

            BusProvider.getBus().post(new ServerFileDeleteEvent(true));

            Snackbar.make(getParentView(), R.string.message_offline_file_deleted, Snackbar.LENGTH_SHORT)
                .show();
        }
    }

    private void stopDownloading(long downloadId) {
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.remove(downloadId);
            BusProvider.getBus().post(new OfflineCanceledEvent(downloadId));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}
