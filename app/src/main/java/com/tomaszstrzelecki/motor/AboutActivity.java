package com.tomaszstrzelecki.motor;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AboutActivity extends AppCompatActivity {

    private int count = 0;
    private Thread UIThread;
    private ImageView downarrow;
    private boolean arrowHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ImageView logo = (ImageView) findViewById(R.id.app_logo);
        downarrow = (ImageView) findViewById(R.id.downarrow);
        startUIThread();
        logo.setOnClickListener(new View.OnClickListener() {
            //@Override11
            public void onClick(View v) {
                if(count == 4) {
                    MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ogar);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.start();
                }
                count++;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startUIThread() {
        UIThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(arrowHidden){
                                    downarrow.setVisibility(View.VISIBLE);
                                    arrowHidden = false;
                                } else {
                                    downarrow.setVisibility(View.GONE);
                                    arrowHidden = true;
                                }
                            }
                        });
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        UIThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIThread.interrupt();
    }
}
