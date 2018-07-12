package org.amahi.anywhere.activity;

import android.Manifest;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.RecentFilesAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileCopiedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.bus.FileOptionClickEvent;
import org.amahi.anywhere.bus.OfflineCanceledEvent;
import org.amahi.anywhere.bus.ServerFileDeleteEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.fragment.PrepareDialogFragment;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static org.amahi.anywhere.fragment.ServerFilesFragment.EXTERNAL_STORAGE_PERMISSION;

public class RecentFilesActivity extends AppCompatActivity implements
    ServerFileClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    EasyPermissions.PermissionCallbacks,
    CastStateListener {

    @Inject
    ServerClient serverClient;
    private List<RecentFile> recentFiles;
    @FileOption.Types
    private int selectedFileOption;
    private int selectedPosition = -1;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_files);

        setUpInjections();
        setUpCast();
        setUpHomeNavigation();
        setUpFilesContentRefreshing();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpCast() {
        mCastContext = CastContext.getSharedInstance(this);
    }

    @Override
    public void onCastStateChanged(int newState) {
        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showIntroductoryOverlay();
        }
    }

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(() -> {
                mIntroductoryOverlay = new IntroductoryOverlay
                    .Builder(this, mediaRouteMenuItem)
                    .setTitleText("Introducing Cast")
                    .setSingleTime()
                    .setOnOverlayDismissedListener(
                        () -> mIntroductoryOverlay = null)
                    .build();
                mIntroductoryOverlay.show();
            });
        }
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        setUpFilesTitle();
    }

    private void setUpFilesTitle() {
        getSupportActionBar().setTitle(R.string.title_recent_files);
    }

    private void setUpRecentFileList() {
        getRecentFileRView().setLayoutManager(new LinearLayoutManager(this));
        addListItemDivider();
        setUpListAdapter();
    }

    private RecyclerView getRecentFileRView() {
        return findViewById(R.id.recent_list);
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
            getRecentFileRView().getContext(),
            DividerItemDecoration.VERTICAL);

        getRecentFileRView().addItemDecoration(dividerItemDecoration);
    }

    private void setUpListAdapter() {
        recentFiles = getRecentFilesList();
        getRecentFileRView().setAdapter(new RecentFilesAdapter(this, recentFiles));
        showList(!recentFiles.isEmpty());
    }

    private List<RecentFile> getRecentFilesList() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return repository.getAllRecentFiles();
    }

    private void showList(boolean notEmpty) {
        if (notEmpty) {
            getRecentFileRView().setVisibility(View.VISIBLE);
            getEmptyView().setVisibility(View.GONE);
        } else {
            getRecentFileRView().setVisibility(View.GONE);
            getEmptyView().setVisibility(View.VISIBLE);
        }
    }

    private LinearLayout getEmptyView() {
        return findViewById(android.R.id.empty);
    }

    private void setUpFilesContentRefreshing() {
        SwipeRefreshLayout refreshLayout = getRefreshLayout();

        refreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light);

        refreshLayout.setOnRefreshListener(this);
    }

    private SwipeRefreshLayout getRefreshLayout() {
        return findViewById(R.id.layout_refresh);
    }

    @Override
    public void onItemClick(View view, int position) {
        startFileOpening(position);
    }

    private void startFileOpening(int position) {
        setRecentFiles(recentFiles.get(position));
        startFileActivity(recentFiles.get(position));
    }

    private void setRecentFiles(RecentFile recentFile) {
        RecentFileRepository repository = new RecentFileRepository(this);
        recentFile.setVisitTime(System.currentTimeMillis());
        repository.insert(recentFile);
    }

    private void startFileActivity(RecentFile file) {
        Intent intent = Intents.Builder.with(this).buildRecentFileIntent(file);
        startActivity(intent);
    }

    @Override
    public void onMoreOptionClick(View view, int position) {
        this.selectedPosition = position;
        ServerFile file = prepareServerFile(recentFiles.get(position));
        Fragments.Builder.buildFileOptionsDialogFragment(this, file)
            .show(getSupportFragmentManager(), "file_options_dialog");
    }

    private ServerFile prepareServerFile(RecentFile recentFile) {
        ServerFile serverFile = new ServerFile(recentFile.getName(), recentFile.getModificationTime(), recentFile.getMime());
        serverFile.setOffline(isFileDownloading(serverFile));
        return serverFile;
    }

    private boolean isFileDownloading(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null;
    }

    private boolean isFileAvailableOffline(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null && file.getState() == OfflineFile.DOWNLOADED;
    }

    @Override
    public void onRefresh() {
        getListAdapter().replaceWith(getRecentFilesList());
        getRefreshLayout().setRefreshing(false);
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

    @Subscribe
    public void onFileOptionSelected(FileOptionClickEvent event) {
        selectedFileOption = event.getFileOption();
        switch (selectedFileOption) {
            case FileOption.DOWNLOAD:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    prepareDownload();
                }
                break;
            case FileOption.SHARE:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    prepareDownload();
                }
                break;
            case FileOption.DELETE:
                deleteFile();
                break;
            case FileOption.OFFLINE_ENABLED:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    changeOfflineState(true);
                }
                break;
            case FileOption.OFFLINE_DISABLED:
                changeOfflineState(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWritePermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            handleFileOptionsWithPermissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.share_permission),
                EXTERNAL_STORAGE_PERMISSION, perms);
        }
    }

    private void handleFileOptionsWithPermissionGranted() {
        switch (selectedFileOption) {
            case FileOption.DOWNLOAD:
                prepareDownload();
                break;
            case FileOption.SHARE:
                prepareDownload();
                break;
            case FileOption.OFFLINE_ENABLED:
                changeOfflineState(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (selectedPosition != -1) {
            handleFileOptionsWithPermissionGranted();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
                showPermissionSnackBar(getString(R.string.share_permission_denied));
            }
        }
    }

    private void showPermissionSnackBar(String message) {
        Snackbar.make(getRecentFileRView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_settings, v -> new AppSettingsDialog.Builder(this).build().show())
            .show();
    }

    private void prepareDownload() {

        ServerFile serverFile = prepareServerFile(recentFiles.get(selectedPosition));

        if (isFileAvailableOffline(serverFile)) {
            prepareDownloadingFile(recentFiles.get(selectedPosition));
        } else {
            startFileDownloading(recentFiles.get(selectedPosition));
        }
    }

    private void prepareDownloadingFile(RecentFile file) {
        PrepareDialogFragment fragment = new PrepareDialogFragment();
        fragment.show(getSupportFragmentManager(), "prepare_dialog");

        File sourceLocation = new File(getFilesDir(), Downloader.OFFLINE_PATH + "/" + file.getName());
        File downloadLocation;
        if (selectedFileOption == FileOption.DOWNLOAD) {
            downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS),
                file.getName());
        } else {
            downloadLocation = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file.getName());
        }

        FileManager.newInstance(this).copyFile(sourceLocation, downloadLocation);
    }

    @Subscribe
    public void onFileCopied(FileCopiedEvent event) {
        Uri contentUri = FileManager.newInstance(this).getContentUri(event.getTargetLocation());

        dismissPreparingDialog();
        finishFileDownloading(contentUri);
    }

    private void startFileDownloading(RecentFile recentFile) {
        showFileDownloadingFragment(prepareServerFile(recentFile));
    }

    private void showFileDownloadingFragment(ServerFile file) {
        DialogFragment fragment = ServerFileDownloadingFragment.newInstance(null, file, FileOption.DOWNLOAD);
        fragment.show(getFragmentManager(), ServerFileDownloadingFragment.TAG);
    }

    private void dismissPreparingDialog() {
        PrepareDialogFragment fragment = (PrepareDialogFragment) getSupportFragmentManager().findFragmentByTag("prepare_dialog");
        if (fragment != null && fragment.isAdded()) {
            fragment.dismiss();
        }
    }

    @Subscribe
    public void onFileDownloaded(FileDownloadedEvent event) {
        finishFileDownloading(event.getFileUri());
    }

    private void finishFileDownloading(Uri fileUri) {
        switch (selectedFileOption) {

            case FileOption.DOWNLOAD:
                showFileDownloadedDialog(getSelectedRecentFile(), fileUri);
                break;

            case FileOption.SHARE:
                startFileSharingActivity(getSelectedRecentFile(), fileUri);
                break;

            default:
                break;
        }
    }

    private void showFileDownloadedDialog(RecentFile recentFile, Uri fileUri) {
        Snackbar.make(getRecentFileRView(), R.string.message_file_download_complete, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_open, view -> startFileOpeningActivity(recentFile, fileUri))
            .show();
    }

    private void startFileOpeningActivity(RecentFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileOpeningIntent(prepareServerFile(file), fileUri);
        startActivity(intent);
    }

    private RecentFile getSelectedRecentFile() {
        return recentFiles.get(selectedPosition);
    }

    private void startFileSharingActivity(RecentFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileSharingIntent(prepareServerFile(file), fileUri);
        startActivity(intent);
    }

    private void deleteFile() {

        new AlertDialog.Builder(this)
            .setTitle(R.string.message_delete_file_title)
            .setMessage(R.string.message_delete_file_body)
            .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                showDeleteDialog();
                serverClient.deleteFile(getSelectedRecentFile().getShareName(), prepareServerFile(getSelectedRecentFile()));
            })
            .setNegativeButton(R.string.button_no, null)
            .show();
    }

    private void showDeleteDialog() {
        PrepareDialogFragment fragment = new PrepareDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Fragments.Arguments.DIALOG_TYPE, PrepareDialogFragment.DELETE_DIALOG);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "prepare_dialog");
    }

    @Subscribe
    public void onFileDeleteEvent(ServerFileDeleteEvent fileDeleteEvent) {
        dismissPreparingDialog();

        if (fileDeleteEvent.isDeleted()) {
            removeFileFromDatabase(getSelectedRecentFile());
            RecentFile recentFile = getSelectedRecentFile();
            getListAdapter().removeFile(selectedPosition);
            recentFiles.remove(recentFile);
            selectedPosition = -1;
        } else {
            Toast.makeText(this, R.string.message_delete_file_error, Toast.LENGTH_SHORT).show();
        }
        showList(!recentFiles.isEmpty());
    }

    private void removeFileFromDatabase(RecentFile recentFile) {
        RecentFileRepository repository = new RecentFileRepository(this);
        repository.deleteFile(recentFile.getUniqueKey());
    }

    private void changeOfflineState(boolean enable) {
        if (enable) {
            startDownloadService(prepareServerFile(getSelectedRecentFile()));
        } else {
            deleteFileFromOfflineStorage();
        }
    }

    private void deleteFileFromOfflineStorage() {
        OfflineFileRepository offlineFileRepository = new OfflineFileRepository(this);
        OfflineFile offlineFile = offlineFileRepository.getOfflineFile(getSelectedRecentFile().getName(), getSelectedRecentFile().getModificationTime());
        if (offlineFile != null) {
            if (offlineFile.getState() == OfflineFile.DOWNLOADING) {
                stopDownloading(offlineFile.getDownloadId());
            }

            File file = getOfflineFileLocation(offlineFile.getName());
            FileManager.newInstance(this).deleteFile(file);

            offlineFileRepository.delete(offlineFile);

            Snackbar.make(getRecentFileRView(), R.string.message_offline_file_deleted, Snackbar.LENGTH_SHORT)
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

    private File getOfflineFileLocation(String name) {
        return new File(getFilesDir(), Downloader.OFFLINE_PATH + "/" + name);
    }

    private void startDownloadService(ServerFile file) {
        Intent downloadService = Intents.Builder.with(this).buildDownloadServiceIntent(file, null);
        startService(downloadService);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCastContext.addCastStateListener(this);
        BusProvider.getBus().register(this);

        setUpRecentFileList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCastContext.removeCastStateListener(this);
        BusProvider.getBus().unregister(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_bar_cast_button, menu);

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
            this.getApplicationContext(),
            menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getListAdapter().tearDownCallbacks();
    }

    private RecentFilesAdapter getListAdapter() {
        return (RecentFilesAdapter) getRecentFileRView().getAdapter();
    }
}
