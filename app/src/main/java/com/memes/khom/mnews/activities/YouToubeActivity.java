package com.memes.khom.mnews.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.memes.khom.memsnews.R;


public class YouToubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    static private final String DEVELOPER_KEY = "44267967193-5tr8kfuangbssq4sf5dkdmimfogis72t.apps.googleusercontent.com";
    private String video;
    static public final String VIDEOTAG = "videoTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
            }
            this.getWindow().setLayout(metrics.widthPixels - 60, (int) (metrics.heightPixels / 1.5));
            Bundle extras = getIntent().getExtras();

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (extras != null) {
                video = extras.getString(VIDEOTAG);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setContentView(R.layout.activity_you_toube);
        YouTubePlayerView youTubeView = findViewById(R.id.youtube_view);
        youTubeView.initialize(DEVELOPER_KEY, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (video != null && !video.isEmpty()) youTubePlayer.loadVideo(video);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
