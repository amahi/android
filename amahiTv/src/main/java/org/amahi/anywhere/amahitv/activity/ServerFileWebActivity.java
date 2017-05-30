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

package org.amahi.anywhere.amahitv.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import org.amahi.anywhere.amahitv.AmahiApplication;
import org.amahi.anywhere.amahitv.R;
import org.amahi.anywhere.amahitv.server.client.ServerClient;
import org.amahi.anywhere.amahitv.server.model.ServerFile;
import org.amahi.anywhere.amahitv.server.model.ServerShare;
import org.amahi.anywhere.amahitv.util.Intents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Web activity. Shows web resources such as SVG and HTML files.
 * Backed up by {@link WebView}.
 */
public class ServerFileWebActivity extends AppCompatActivity
{
	private static final Set<String> SUPPORTED_FORMATS;
	CustomTabsClient mCustomTabsClient;
	CustomTabsSession mCustomTabsSession;
	CustomTabsServiceConnection mCustomTabsServiceConnection;
	CustomTabsIntent mCustomTabsIntent;

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

		setUpWebResource(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpWebResource(Bundle state) {
		setUpWebResourceContent(state);
	}

	private void setUpWebResourceContent(Bundle state) {
		if (!isWebResourceStateValid(state)){
			setUpCustomTabs();
		}
	}

	private void setUpCustomTabs(){
		startActivity(new Intent(this,WebActivity.class).putExtra(Intent.EXTRA_TEXT,getWebResourceUri().toString()));
	}

	private boolean isWebResourceStateValid(Bundle state) {
		return state != null;
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

	public static boolean supports(String mime_type) {
		return SUPPORTED_FORMATS.contains(mime_type);
	}

	@Override
	protected void onResume() {
		super.onResume();
		onBackPressed();
	}
}
