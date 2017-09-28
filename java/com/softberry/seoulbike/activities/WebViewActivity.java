package com.softberry.seoulbike.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.softberry.seoulbike.R;

/**
 * Created by parkjs on 2016-10-14.
 */
public class WebViewActivity extends Activity implements View.OnClickListener {

    private Button mBackKey;
    private TextView mTitle;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initView();
        initData();
    }

    private void initView() {
        mBackKey = (Button) findViewById(R.id.btn_back);
        mBackKey.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.action_title);
        mWebView = (WebView) findViewById(R.id.web_view);
    }

    private void initData() {
        Intent intent = getIntent();
        String title = intent.getExtras().getString("title");
        String path = intent.getExtras().getString("path");
        mTitle.setText(title);
        mWebView.loadUrl(path);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
        }

    }


}
