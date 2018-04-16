package org.amahi.anywhere.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.amahi.anywhere.R;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpHomeNavigation();
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webview);

        loadWebView("https://www.amahi.org/android");

    }

    private void loadWebView(String url) {
        webView.setWebViewClient(new WebViewClient());

        WebSettings settings = webView.getSettings();

        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);

        webView.loadUrl(url);

    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
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
