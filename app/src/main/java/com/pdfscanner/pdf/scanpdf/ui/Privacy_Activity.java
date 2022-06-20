package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pdfscanner.pdf.scanpdf.R;


public class Privacy_Activity extends AppCompatActivity {

    private WebView privacy_web;
    private ProgressBar privacy_progress;
    Toolbar toolbar;
    private ImageView iv_close;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        setContentView(R.layout.activity_privacy);
        initView();
    }

    private void initView() {
        iv_close = findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        privacy_progress = (ProgressBar)findViewById(R.id.privacy_progress);
        privacy_web = (WebView) findViewById(R.id.privacy_web);
        privacy_web.setWebViewClient(new MyWebViewClient());
        openURL();

    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            privacy_web.setVisibility(View.VISIBLE);
        }
    }

    public void openURL() {
        privacy_web.loadUrl(getResources().getString(R.string.privacy_url));
        privacy_web.requestFocus();
    }

}