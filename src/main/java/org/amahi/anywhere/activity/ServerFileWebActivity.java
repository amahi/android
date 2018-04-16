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

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v7.app.AppCompatActivity;

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

/**
 * Web activity. Shows web resources such as SVG and HTML files.
 * Backed up by {@link android.webkit.WebView}.
 */
public class ServerFileWebActivity extends AppCompatActivity {
    private static final Set<String> SUPPORTED_FORMATS;

    static {
        SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
            "image/svg+xml",
            "text/html",
            "text/plain"
        ));
    }

    CustomTabsClient mCustomTabsClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent mCustomTabsIntent;
    @Inject
    ServerClient serverClient;

    public static boolean supports(String mime_type) {
        return SUPPORTED_FORMATS.contains(mime_type);
    }

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
        if (!isWebResourceStateValid(state)) {
            setUpCustomTabs();
        }
    }

    private void setUpCustomTabs() {

        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(this, getPackageName(), mCustomTabsServiceConnection);

        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
            .setShowTitle(true)
            .build();

        mCustomTabsIntent.launchUrl(this, getWebResourceUri());
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

    @Override
    protected void onResume() {
        super.onResume();
        onBackPressed();
    }
}
