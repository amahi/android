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

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.ViewDirector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Web activity. Shows web resources such as SVG and HTML files.
 * Backed up by {@link android.webkit.WebView}.
 */
public class ServerFileWebActivity extends AppCompatActivity
{
	private static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"image/svg+xml",
			"text/html",
			"text/plain"
		));
	}

	@Inject
	ServerClient serverClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_web);

		setUpInjections();

		setUpHomeNavigation();

		setUpWebResource(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	private void setUpWebResource(Bundle state) {
		setUpWebResourceTitle();
		setUpWebResourceClient();
		setUpWebResourceContent(state);
	}

	private void setUpWebResourceTitle() {
		getSupportActionBar().setTitle(getFile().getName());
	}

	private void setUpWebResourceClient() {
		getWebView().setWebViewClient(new WebResourceClient(this));
	}

	private void setUpWebResourceContent(Bundle state) {
		if (!isWebResourceStateValid(state)) {
			getWebView().loadUrl(getWebResourceUri().toString());
		}
	}

	private boolean isWebResourceStateValid(Bundle state) {
		return state != null;
	}

	private WebView getWebView() {
		return (WebView) findViewById(R.id.web_content);
	}

	private Uri getWebResourceUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private ServerFile getFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		setUpWebResourceState(savedInstanceState);
	}

	private void setUpWebResourceState(Bundle state) {
		getWebView().restoreState(state);
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownWebResourceState(outState);
	}

	private void tearDownWebResourceState(Bundle state) {
		getWebView().saveState(state);
	}

	public static boolean supports(String mime_type) {
		return SUPPORTED_FORMATS.contains(mime_type);
	}

	private static final class WebResourceClient extends WebViewClient
	{
		private final ServerFileWebActivity activity;

		public WebResourceClient(ServerFileWebActivity activity) {
			this.activity = activity;
		}

		@Override
		public void onPageStarted(WebView appWebView, String appUrl, Bitmap appFavicon) {
			super.onPageStarted(appWebView, appUrl, appFavicon);

			activity.showProgress();
		}

		@Override
		public void onPageFinished(WebView appWebView, String appUrl) {
			super.onPageFinished(appWebView, appUrl);

			activity.showApp();
		}
	}

	private void showProgress() {
		ViewDirector.of(this, R.id.animator).show(android.R.id.progress);
	}

	private void showApp() {
		ViewDirector.of(this, R.id.animator).show(R.id.web_content);
	}
}
