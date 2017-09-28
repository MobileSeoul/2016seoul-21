package com.softberry.seoulbike.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.softberry.seoulbike.R;

/**
 * Created by parkjs
 */
public class IntroActivity extends Activity {

    private final String TAG = "IntroActivity";

    private final static int HANDLER_FINISH_INTRO =1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case HANDLER_FINISH_INTRO:
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        mHandler.sendEmptyMessageDelayed(HANDLER_FINISH_INTRO, 1000);
    }
}
