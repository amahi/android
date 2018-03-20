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

package org.amahi.anywhere.tv.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Intents;

import javax.inject.Inject;

/**
 * Web activity. Shows web resources such as SVG and HTML files.
 * Backed up by {@link android.webkit.WebView}.
 */

public class TVWebViewActivity extends Activity {
    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_web);
        setUpInjections();
        setWebPage();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setWebPage() {
        WebView webView = findViewById(R.id.tv_web_view);
        webView.loadUrl(String.valueOf(getWebResourceUri()));
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
}
