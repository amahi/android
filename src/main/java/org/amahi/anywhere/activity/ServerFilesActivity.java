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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.fragment.GooglePlaySearchFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.util.List;

import javax.inject.Inject;

/**
 * Files activity. Shows files navigation and operates basic file actions,
 * such as opening and sharing.
 * The files navigation itself is done via {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesActivity extends AppCompatActivity
{
	private static final class State
	{
		private State() {
		}

		public static final String FILE = "file";
		public static final String FILE_ACTION = "file_action";
	}

	private static enum FileAction
	{
		OPEN, SHARE
	}

	@Inject
	ServerClient serverClient;

	private ServerFile file;
	private FileAction fileAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_files);

		setUpInjections();

		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

		setUpHomeNavigation();

		setUpFiles(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private void setUpFiles(Bundle state) {
		setUpFilesTitle();
		setUpFilesFragment();
		setUpFilesState(state);
	}

	private void setUpFilesTitle() {
		getSupportActionBar().setTitle(getShare().getName());
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
				finish();
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
