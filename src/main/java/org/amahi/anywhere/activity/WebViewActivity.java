package org.amahi.anywhere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.amahi.anywhere.R;

public class WebViewActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpHomeNavigation();
        setContentView(R.layout.activity_web_view);
        webView=(WebView)findViewById(R.id.webview);

        loadWebView("https://www.amahi.org/android");

    }

    private void loadWebView(String url){
        webView.setWebViewClient(new WebViewClient());

        WebSettings settings=webView.getSettings();

        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);

        webView.loadUrl(url);

    }

    private void setUpHomeNavigation() {
        getActionBar().setHomeButtonEnabled(true);
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