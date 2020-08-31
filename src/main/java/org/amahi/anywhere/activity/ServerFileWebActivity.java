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
import android.content.Context;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.appcompat.app.AppCompatActivity;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.db.entities.FileInfo;
import org.amahi.anywhere.db.repositories.FileInfoRepository;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.DateTime;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.LocaleHelper;

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

    AsyncTask asyncTask;

    public static boolean supports(String mime_type) {
        return SUPPORTED_FORMATS.contains(mime_type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_file_web);

        setUpInjections();

        setUpWebResource(savedInstanceState);

        setUpLastOpened();
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
            .setToolbarColor(getResources().getColor(R.color.primary))
            .setShowTitle(true)
            .build();

        mCustomTabsIntent.launchUrl(this, getWebResourceUri());
    }

    private void setUpLastOpened() {
        FileInfoRepository fileInfoRepository = new FileInfoRepository(this);
        FileInfo fileInfo = new FileInfo(getFile().getUniqueKey(), DateTime.getCurrentTime());
        fileInfoRepository.insert(fileInfo);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocationListener.get().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncTask.cancel(true);
    }

}
