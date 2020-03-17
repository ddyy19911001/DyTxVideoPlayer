package com.jujing.dytxvideoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.MySuperVideoView;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout fmTop;
    private MySuperVideoView videoView;
    private ImageView mIvPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        showPauseWaitPlay(true);
    }

    private void findView() {
        fmTop = (RelativeLayout) findViewById(R.id.fm_top);
        videoView = (MySuperVideoView) findViewById(R.id.video_view);
        mIvPlay = (ImageView) findViewById(R.id.m_iv_play);
    }

    /**
     * 是否显示暂停播放图标
     * @param isShow
     */
    private void showPauseWaitPlay(boolean isShow) {
        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.getmVodPlayer()!=null&&videoView.getmVodPlayer().getPlayableDuration()>0){
                    videoView.onResume();
                }else {
                    videoView.startPlay("https://youku.cdn7-okzy.com/20200313/17725_bd26d414/index.m3u8");
                }
                mIvPlay.setVisibility(View.GONE);
            }
        });
        if(isShow){
            mIvPlay.setVisibility(View.VISIBLE);
        }else{
            if(videoView.getmVodPlayer()!=null&&videoView.getmVodPlayer().getPlayableDuration()>0){
                videoView.onResume();
            }else {
                videoView.startPlay("https://youku.cdn7-okzy.com/20200313/17725_bd26d414/index.m3u8");
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if(videoView!=null&&videoView.getmVodPlayer()!=null) {
            videoView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(videoView!=null&&videoView.getmVodPlayer()!=null) {
            videoView.onResume();
        }
    }


    @Override
    public void onDestroy() {
        if(videoView!=null&&videoView.getmVodPlayer()!=null){
            videoView.onPause();
            videoView.getmVodPlayer().stopPlay(true);
            videoView.release();
            videoView=null;
        }
        super.onDestroy();
    }


}
