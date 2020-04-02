package com.jujing.dytxvideoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.BaseVideoActivity;
import com.tencent.liteav.demo.play.MySuperVideoView;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.utils.ScreenSwitchUtils;

public class MainActivity extends BaseVideoActivity {
    private RelativeLayout fmTop;
    private MySuperVideoView videoView;
    private FrameLayout mIvPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initAllVideo();
        SuperPlayerModel superPlayerModel=new SuperPlayerModel();
        superPlayerModel.title="";
        superPlayerModel.url="https://youku.cdn7-okzy.com/20200313/17725_bd26d414/index.m3u8";
        play(true,superPlayerModel );
    }

    private void findView() {
        fmTop = (RelativeLayout) findViewById(R.id.fm_top);
        videoView = (MySuperVideoView) findViewById(R.id.video_view);
        mIvPlay = (FrameLayout) findViewById(R.id.fm_iv_play);
    }


    @Override
    public View getPauseView() {
        return mIvPlay;
    }

    @Override
    public MySuperVideoView getVideoView() {
        return videoView;
    }
}
