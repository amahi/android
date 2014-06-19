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
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Intents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileImageActivity extends Activity implements Callback
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"image/bmp",
			"image/jpeg",
			"image/gif",
			"image/png",
			"image/webp"
		));
	}

	@Inject
	ServerClient serverClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_image);

		setUpInjections();

		setUpHomeNavigation();

		setUpImage();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getActionBar().setHomeButtonEnabled(true);
	}

	private void setUpImage() {
		setUpImageTitle();
		setUpImageContent();
	}

	private void setUpImageTitle() {
		getActionBar().setTitle(getFile().getName());
	}

	private void setUpImageContent() {
		Picasso
			.with(this)
			.load(getImageUri())
			.fit()
			.centerInside()
			.into(getImageView(), this);
	}

	private Uri getImageUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private ServerFile getFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private ImageView getImageView() {
		return (ImageView) findViewById(R.id.image);
	}

	@Override
	public void onSuccess() {
		showImageContent();
	}

	private void showImageContent() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(R.id.image)));
	}

	@Override
	public void onError() {
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
}
