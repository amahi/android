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
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Intents;

import java.util.Locale;

import javax.inject.Inject;

public class ServerAppActivity extends Activity
{
	@Inject
	ServerClient serverClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_web);

		setUpInjections();

		setUpApp();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpApp() {
		setUpAppWebAgent();
		setUpAppWebClient();
		setUpAppWebSettings();
		setUpAppWebTitle();
		setUpAppWebContent();
	}

	private void setUpAppWebAgent() {
		getWebView().getSettings().setUserAgentString(getAppWebAgent());
	}

	private WebView getWebView() {
		return (WebView) findViewById(R.id.web_content);
	}

	private String getAppWebAgent() {
		return String.format(Locale.US, "AmahiAnywhere/%s (Android %s; %s) Size/%.1f Resolution/%dx%d Vhost/%s",
			Android.getApplicationVersion(),
			Android.getVersion(),
			Android.getDeviceName(),
			Android.getDeviceScreenSize(this),
			Android.getDeviceScreenHeight(this),
			Android.getDeviceScreenWidth(this),
			getApp().getHost());
	}

	private ServerApp getApp() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_APP);
	}

	private void setUpAppWebClient() {
		getWebView().setWebViewClient(new WebViewClient());
	}

	private void setUpAppWebSettings() {
		getWebView().getSettings().setJavaScriptEnabled(true);
	}

	private void setUpAppWebTitle() {
		getActionBar().setTitle(getApp().getName());
	}

	private void setUpAppWebContent() {
		getWebView().loadUrl(serverClient.getServerAddress());
	}
}
