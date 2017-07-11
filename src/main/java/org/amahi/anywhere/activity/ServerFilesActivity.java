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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.bus.ServerFileUploadCompleteEvent;
import org.amahi.anywhere.bus.ServerFileUploadProgressEvent;
import org.amahi.anywhere.bus.UploadClickEvent;
import org.amahi.anywhere.fragment.GooglePlaySearchFragment;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.fragment.UploadBottomSheet;
import org.amahi.anywhere.model.UploadOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Files activity. Shows files navigation and operates basic file actions,
 * such as opening and sharing.
 * The files navigation itself is done via {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	private static final class State {

		private State() {
		}

		public static final String FILE = "file";

		public static final String FILE_ACTION = "file_action";
	}


	private enum FileAction {
		OPEN, SHARE;
	}

	@Inject
	ServerClient serverClient;

	private ServerFile file;

	private FileAction fileAction;
	private ProgressDialog uploadProgressDialog;

	private static final int FILE_UPLOAD_PERMISSION = 102;
	private static final int CAMERA_PERMISSION = 103;
	private static final int RESULT_UPLOAD_IMAGE = 201;
	private static final int RESULT_CAMERA_IMAGE = 202;

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
		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_upload);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new UploadBottomSheet().show(getSupportFragmentManager(), "upload_dialog");
			}
		});
	}

	private void setUpUploadDialog() {
		uploadProgressDialog = new ProgressDialog(this);
		uploadProgressDialog.setTitle(getString(R.string.message_file_upload_title));
		uploadProgressDialog.setCancelable(false);
		uploadProgressDialog.setIndeterminate(false);
		uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
			this.fileAction = (FileAction) state.getSerializable(State.FILE_ACTION);
		}
	}

	private boolean isFilesStateValid(Bundle state) {
		return (state != null) && state.containsKey(State.FILE) && state.containsKey(State.FILE_ACTION);
	}

	@Subscribe
	public void onFileOpening(FileOpeningEvent event) {
		this.file = event.getFile();
		this.fileAction = FileAction.OPEN;

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
		DialogFragment fragment = ServerFileDownloadingFragment.newInstance(share, file);
		fragment.show(getFragmentManager(), ServerFileDownloadingFragment.TAG);
	}

	@Subscribe
	public void onFileDownloaded(FileDownloadedEvent event) {
		finishFileDownloading(event.getFileUri());
	}

	private void finishFileDownloading(Uri fileUri) {
		switch (fileAction) {
			case OPEN:
				startFileOpeningActivity(file, fileUri);
				break;

			case SHARE:
				startFileSharingActivity(file, fileUri);
				break;

			default:
				break;
		}
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

	private void showPermissionSnackBar(String message) {
		Snackbar.make(findViewById(R.id.coordinator_files), message, Snackbar.LENGTH_LONG)
				.setAction(R.string.menu_settings, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AppSettingsDialog.Builder(ServerFilesActivity.this).build().show();
					}
				})
				.show();
	}

	@Subscribe
	public void onUploadOptionClick(UploadClickEvent event) {
		int option = event.getUploadOption();
		switch (option) {
			case UploadOption.CAMERA:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					checkCameraPermissions();
				} else {
					openCamera();
				}
				break;
			case UploadOption.FILE:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					checkFileReadPermissions();
				} else {
					showFileChooser();
				}
				break;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private void checkCameraPermissions() {
		String[] perms = {Manifest.permission.CAMERA};
		if (EasyPermissions.hasPermissions(this, perms)) {
			openCamera();
		} else {
			EasyPermissions.requestPermissions(this, getString(R.string.camera_permission),
					CAMERA_PERMISSION, perms);
		}
	}

	private void openCamera() {
		Intent intent = Intents.Builder.with(this).buildCameraIntent();
		startActivityForResult(intent, RESULT_CAMERA_IMAGE);
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
		this.startActivityForResult(intent, RESULT_UPLOAD_IMAGE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case RESULT_UPLOAD_IMAGE:
					if (data != null) {
						Uri selectedImageUri = data.getData();
						String filePath = querySelectedImagePath(selectedImageUri);
						if (filePath != null) {
							File file = new File(filePath);
							if (checkForDuplicateFile(file.getName())) {
								showDuplicateFileUploadDialog(file);
							} else {
								uploadFile(file);
							}
						}
					}
					break;
				case RESULT_CAMERA_IMAGE:

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
				.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						uploadFile(file);
					}
				})
				.setNegativeButton(R.string.button_no, null)
				.show();
	}

	private void uploadFile(File uploadFile) {
		if (!isDirectory(file)) {
			serverClient.uploadFile(uploadFile, getShare());
		} else {
			serverClient.uploadFile(uploadFile, getShare(), file);
		}
		uploadProgressDialog.show();
	}

	private boolean checkForDuplicateFile(String fileName) {
		List<ServerFile> files;
//		if (!isMetadataAvailable()) {
//			files = getFilesAdapter().getItems();
//		} else {
//			files = getFilesAdapter().getItems();
//		}
//		for (ServerFile serverFile : files) {
//			if (serverFile.getName().equals(fileName)) {
//				return true;
//			}
//		}
		return false;
	}

	@Subscribe
	public void onFileUploadProgressEvent(ServerFileUploadProgressEvent fileUploadProgressEvent) {
		if (uploadProgressDialog.isShowing()) {
			uploadProgressDialog.setProgress(fileUploadProgressEvent.getProgress());
		}
	}

	@Subscribe
	public void onFileUploadCompleteEvent(ServerFileUploadCompleteEvent fileUploadCompleteEvent) {
		uploadProgressDialog.dismiss();
		if (fileUploadCompleteEvent.wasUploadSuccessful()) {
//			if (!isMetadataAvailable()) {
//				getFilesAdapter().notifyDataSetChanged();
//			} else {
//				getFilesAdapter().notifyDataSetChanged();
//			}
			Toast.makeText(this, R.string.message_file_upload_complete, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.message_file_upload_error, Toast.LENGTH_SHORT).show();
		}
	}

	@Subscribe
	public void onFileSharing(ServerFileSharingEvent event) {
		this.file = event.getFile();
		this.fileAction = FileAction.SHARE;

		startFileSharingActivity(event.getShare(), event.getFile());
	}

	private void startFileSharingActivity(ServerShare share, ServerFile file) {
		startFileDownloading(share, file);
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
		state.putSerializable(State.FILE_ACTION, fileAction);
	}
}
