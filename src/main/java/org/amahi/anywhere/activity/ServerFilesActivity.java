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

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileCopiedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFileDownloadingEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.bus.ServerFileUploadCompleteEvent;
import org.amahi.anywhere.bus.ServerFileUploadProgressEvent;
import org.amahi.anywhere.bus.UploadClickEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.fragment.GooglePlaySearchFragment;
import org.amahi.anywhere.fragment.PrepareDialogFragment;
import org.amahi.anywhere.fragment.ProgressDialogFragment;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.fragment.UploadBottomSheet;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.model.UploadOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Files activity. Shows files navigation and operates basic file actions,
 * such as opening and sharing.
 * The files navigation itself is done via {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int FILE_UPLOAD_PERMISSION = 102;
    private static final int CAMERA_PERMISSION = 103;
    private static final int REQUEST_UPLOAD_IMAGE = 201;
    private static final int REQUEST_CAMERA_IMAGE = 202;
    @Inject
    ServerClient serverClient;
    private ServerFile file;
    @FileOption.Types
    private int fileOption;
    private ProgressDialogFragment uploadDialogFragment;
    private File cameraImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_files);

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
        setUpUploadFAB();
        setUpUploadDialog();
        setUpFilesFragment();
        setUpFilesState(state);
    }

    private void setUpFilesTitle() {
        getSupportActionBar().setTitle(getShare().getName());
    }

    private void setUpUploadFAB() {
        final FloatingActionButton fab = findViewById(R.id.fab_upload);
        fab.setOnClickListener(view -> new UploadBottomSheet().show(getSupportFragmentManager(), "upload_dialog"));
    }

    private void setUpUploadDialog() {
        uploadDialogFragment = (ProgressDialogFragment) getFragmentManager().findFragmentByTag("progress_dialog");
        if (uploadDialogFragment == null) {
            uploadDialogFragment = new ProgressDialogFragment();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setUpFilesTitle();
    }

    private ServerShare getShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private void setUpFilesFragment() {
        Fragments.Operator.at(this).set(buildFilesFragment(getShare(), null), R.id.container_files);
    }

    private Fragment buildFilesFragment(ServerShare share, ServerFile directory) {
        return Fragments.Builder.buildServerFilesFragment(share, directory);
    }

    private void setUpFilesState(Bundle state) {
        if (isFilesStateValid(state)) {
            this.file = state.getParcelable(State.FILE);
            this.fileOption = state.getInt(State.FILE_ACTION);
        }
    }

    private boolean isFilesStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.FILE) && state.containsKey(State.FILE_ACTION);
    }

    @Subscribe
    public void onFileOpening(FileOpeningEvent event) {
        this.file = event.getFile();
        this.fileOption = FileOption.OPEN;

        setUpFile(event.getShare(), event.getFiles(), event.getFile());
    }

    private void setUpFile(ServerShare share, List<ServerFile> files, ServerFile file) {
        if (isDirectory(file)) {
            setUpFilesFragment(share, file);
        } else {
            setUpFileActivity(share, files, file);
        }
    }

    private boolean isDirectory(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
    }

    private void setUpFilesFragment(ServerShare share, ServerFile directory) {
        Fragments.Operator.at(this).replaceBackstacked(buildFilesFragment(share, directory), R.id.container_files);
    }

    private void setUpFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        if (Intents.Builder.with(this).isServerFileSupported(file)) {
            startFileActivity(share, files, file);
            return;
        }

        if (Intents.Builder.with(this).isServerFileOpeningSupported(file)) {
            startFileOpeningActivity(share, file);
            return;
        }

        showGooglePlaySearchFragment(file);
    }

    private void startFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        Intent intent = Intents.Builder.with(this).buildServerFileIntent(share, files, file);
        startActivity(intent);
    }

    private void startFileOpeningActivity(ServerShare share, ServerFile file) {
        startFileDownloading(share, file);
    }

    private void startFileDownloading(ServerShare share, ServerFile file) {
        showFileDownloadingFragment(share, file);
    }

    private void showFileDownloadingFragment(ServerShare share, ServerFile file) {
        DialogFragment fragment = ServerFileDownloadingFragment.newInstance(share, file, fileOption);
        fragment.show(getFragmentManager(), ServerFileDownloadingFragment.TAG);
    }

    @Subscribe
    public void onFileDownloaded(FileDownloadedEvent event) {
        finishFileDownloading(event.getFileUri());
    }

    private void finishFileDownloading(Uri fileUri) {
        switch (fileOption) {
            case FileOption.OPEN:
                startFileOpeningActivity(file, fileUri);
                break;

            case FileOption.DOWNLOAD:
                showFileDownloadedDialog(file, fileUri);
                break;

            case FileOption.SHARE:
                startFileSharingActivity(file, fileUri);
                break;

            default:
                break;
        }
    }

    private void showFileDownloadedDialog(ServerFile file, Uri fileUri) {
        Snackbar.make(getParentView(), R.string.message_file_download_complete, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_open, view -> startFileOpeningActivity(file, fileUri))
            .show();
    }

    private void startFileOpeningActivity(ServerFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileOpeningIntent(file, fileUri);
        startActivity(intent);
    }

    private void startFileSharingActivity(ServerFile file, Uri fileUri) {
        Intent intent = Intents.Builder.with(this).buildServerFileSharingIntent(file, fileUri);
        startActivity(intent);
    }

    private void showGooglePlaySearchFragment(ServerFile file) {
        GooglePlaySearchFragment fragment = GooglePlaySearchFragment.newInstance(file);
        fragment.show(getFragmentManager(), GooglePlaySearchFragment.TAG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == FILE_UPLOAD_PERMISSION) {
            showFileChooser();
        } else if (requestCode == CAMERA_PERMISSION) {
            openCamera();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == FILE_UPLOAD_PERMISSION) {
            showPermissionSnackBar(getString(R.string.file_upload_permission_denied));
        } else if (requestCode == CAMERA_PERMISSION) {
            showPermissionSnackBar(getString(R.string.file_upload_permission_denied));
        }
    }

    private View getParentView() {
        return findViewById(R.id.coordinator_files);
    }

    private void showPermissionSnackBar(String message) {
        Snackbar.make(getParentView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_settings, v -> new AppSettingsDialog.Builder(ServerFilesActivity.this).build().show())
            .show();
    }

    @Subscribe
    public void onUploadOptionClick(UploadClickEvent event) {
        int option = event.getUploadOption();
        switch (option) {
            case UploadOption.CAMERA:
                if (Android.isPermissionRequired()) {
                    checkCameraPermissions();
                } else {
                    openCamera();
                }
                break;
            case UploadOption.FILE:
                if (Android.isPermissionRequired()) {
                    checkFileReadPermissions();
                } else {
                    showFileChooser();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCameraPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            openCamera();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_permission),
                CAMERA_PERMISSION, perms);
        }
    }

    private void openCamera() {
        Intent cameraIntent = Intents.Builder.with(this).buildCameraIntent();
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraImage = null;
            try {
                cameraImage = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(this,
                    "org.amahi.anywhere.fileprovider", cameraImage);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CAMERA_IMAGE);
            } catch (IOException ex) {
                Timber.d(ex);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(new Date().getTime());
        String imageFileName = "photo-" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkFileReadPermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            showFileChooser();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.file_upload_permission),
                FILE_UPLOAD_PERMISSION, perms);
        }
    }

    private void showFileChooser() {
        Intent intent = Intents.Builder.with(this).buildMediaPickerIntent();
        this.startActivityForResult(intent, REQUEST_UPLOAD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_UPLOAD_IMAGE:
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        String filePath = querySelectedImagePath(selectedImageUri);
                        if (filePath != null) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                ServerFilesFragment fragment = (ServerFilesFragment)
                                    getSupportFragmentManager()
                                        .findFragmentById(R.id.container_files);
                                if (fragment.checkForDuplicateFile(file.getName())) {
                                    showDuplicateFileUploadDialog(file);
                                } else {
                                    uploadFile(file);
                                }
                            }
                        }
                    }
                    break;
                case REQUEST_CAMERA_IMAGE:
                    if (cameraImage.exists()) {
                        uploadFile(cameraImage);
                    }
                    break;
            }
        }
    }

    private String querySelectedImagePath(Uri selectedImageUri) {
        String filePath = null;
        if ("content".equals(selectedImageUri.getScheme())) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver()
                .query(selectedImageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else {
            filePath = selectedImageUri.toString();
        }
        return filePath;
    }

    private void showDuplicateFileUploadDialog(final File file) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.message_duplicate_file_upload)
            .setMessage(getString(R.string.message_duplicate_file_upload_body, file.getName()))
            .setPositiveButton(R.string.button_yes, (dialog, which) -> uploadFile(file))
            .setNegativeButton(R.string.button_no, null)
            .show();
    }

    private void uploadFile(File uploadFile) {
        serverClient.uploadFile(0, uploadFile, getShare(), file);
        uploadDialogFragment.show(getFragmentManager(), "progress_dialog");
    }

    @Subscribe
    public void onFileUploadProgressEvent(ServerFileUploadProgressEvent fileUploadProgressEvent) {
        if (uploadDialogFragment.isAdded()) {
            uploadDialogFragment.setProgress(fileUploadProgressEvent.getProgress());
        }
    }

    @Subscribe
    public void onFileUploadCompleteEvent(ServerFileUploadCompleteEvent event) {

        if(uploadDialogFragment.isAdded())
        uploadDialogFragment.dismiss();
        if (event.wasUploadSuccessful()) {
            Fragments.Operator.at(this).replace(buildFilesFragment(getShare(), file), R.id.container_files);
            Snackbar.make(getParentView(), R.string.message_file_upload_complete, Snackbar.LENGTH_LONG).show();
            if (cameraImage != null && cameraImage.exists()) {
                clearCameraImage();
            }
        } else {
            Snackbar snackbar = Snackbar.make(getParentView(), R.string.message_file_upload_error, Snackbar.LENGTH_LONG);
            if (cameraImage != null && cameraImage.exists()) {
                snackbar
                    .setAction(R.string.button_retry, v -> uploadFile(cameraImage))
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION) {
                                clearCameraImage();
                            }
                        }
                    });
            }
            snackbar.show();
        }
    }

    private void clearCameraImage() {
        //noinspection ResultOfMethodCallIgnored
        cameraImage.delete();
        cameraImage = null;
    }

    @Subscribe
    public void onFileDownloading(ServerFileDownloadingEvent event) {
        this.file = event.getFile();
        this.fileOption = FileOption.DOWNLOAD;

        if(isFileAvailableOffline(event.getFile())) {
            prepareDownloadingFile(file);
        } else {
            startFileDownloading(event.getShare(), file);
        }
    }

    private void prepareDownloadingFile(ServerFile file) {
        PrepareDialogFragment fragment = new PrepareDialogFragment();
        fragment.show(getSupportFragmentManager(), "prepare_dialog");

        File sourceLocation = new File(getFilesDir(), Downloader.OFFLINE_PATH + "/" + file.getName());
        File downloadLocation = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS),
            file.getName());
        FileManager.newInstance(this).copyFile(sourceLocation, downloadLocation);
    }

    @Subscribe
    public void onFileCopied(FileCopiedEvent event) {
        Uri contentUri = FileManager.newInstance(this).getContentUri(event.getTargetLocation());

        dismissPreparingDialog();
        BusProvider.getBus().post(new FileDownloadedEvent(contentUri));
    }

    private void dismissPreparingDialog() {
        PrepareDialogFragment fragment = (PrepareDialogFragment) getSupportFragmentManager().findFragmentByTag("prepare_dialog");
        if(fragment !=null && fragment.isAdded()) {
            fragment.dismiss();
        }
    }

    @Subscribe
    public void onFileSharing(ServerFileSharingEvent event) {
        this.file = event.getFile();
        this.fileOption = FileOption.SHARE;

        startFileSharingActivity(event.getShare(), event.getFile());
    }

    private void startFileSharingActivity(ServerShare share, ServerFile file) {
        if(isFileAvailableOffline(file)) {
            Uri contenUri = FileManager.newInstance(this).getContentUriForOfflineFile(file.getName());
            BusProvider.getBus().post(new FileDownloadedEvent(contenUri));
        } else {
            startFileDownloading(share, file);
        }
    }

    private boolean isFileAvailableOffline(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null && file.getState() == OfflineFile.DOWNLOADED;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tearDownFilesState(outState);
    }

    private void tearDownFilesState(Bundle state) {
        state.putParcelable(State.FILE, file);
        state.putInt(State.FILE_ACTION, fileOption);
    }

    private static final class State {

        public static final String FILE = "file";
        public static final String FILE_ACTION = "file_action";

        private State() {
        }
    }
}
