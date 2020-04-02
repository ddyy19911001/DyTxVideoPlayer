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

import com.tencent.liteav.demo.play.MySuperVideoView;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.utils.ScreenSwitchUtils;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout fmTop;
    private MySuperVideoView videoView;
    private FrameLayout mIvPlay;
    private ScreenSwitchUtils instance;

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
        mIvPlay = (FrameLayout) findViewById(R.id.fm_iv_play);
        instance=new ScreenSwitchUtils(this.getApplicationContext());
        initOritationListener();
    }



    /***
     * 此处开始配置旋转切换全屏逻辑******************************************************************
     */

    @Override
    public void onStart() {
        super.onStart();
        instance.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        instance.stop();
    }

    private void initOritationListener() {
        instance.setListener(new ScreenSwitchUtils.OnOritationChangeListener() {
            @Override
            public void onOritationChange(int orientation) {
                Log.i("方向变化：", ""+orientation);
                if(!needOverride()){
                    return;
                }
                if (orientation > 45 && orientation < 135) {
                    if (instance.isPortrait&&needLandSpace()) {
                        //切换成横屏反向：ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        instance.isPortrait = false;
                    }
                } else if (orientation > 135 && orientation < 225) {
                    if (!instance.isPortrait) {
                        /*
                         * 切换成竖屏反向：ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT(9),
                         * ActivityInfo.SCREEN_ORIENTATION_SENSOR:根据重力感应自动旋转
                         * 此处正常应该是上面第一个属性，但是在真机测试时显示为竖屏正向，所以用第二个替代
                         */
//                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                        instance.isPortrait = true;
                    }
                } else if (orientation > 225 && orientation < 315) {
                    if (instance.isPortrait&&needLandSpace()) {
                        //切换成横屏：ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        instance.isPortrait = false;
                    }
                } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                    if (!instance.isPortrait) {
                        //切换成竖屏ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        instance.isPortrait = true;
                    }
                }
            }
        });
    }

    private boolean needOverride() {
        //方向锁定状态不可进行切换
        try {
            int screenchange = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            if(screenchange==0){
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //横屏锁定状态不可自动切换
        if(videoView!=null
                &&videoView.getPlayMode()==2
                &&videoView.mVodControllerLarge!=null
                &&videoView.mVodControllerLarge.isLockScreen()){
            return false;
        }
        if(videoView==null){
            return false;
        }
        if(videoView.mVodControllerLarge!=null){
            if(videoView.mVodControllerLarge.isClickFullScreen){
                videoView.mVodControllerLarge.setClickFullScreen(false);
                return false;
            }
        }
        if(videoView.mVodControllerSmall!=null){
            if(videoView.mVodControllerSmall.isClickFullScreen){
                videoView.mVodControllerSmall.setClickFullScreen(false);
                return false;
            }
        }
        return true;
    }

    /**
     * 是否需要进行横屏显示
     * @return
     */
    private boolean needLandSpace() {
        if(videoView!=null&&videoView.mVodController!=null&&videoView.getPlayMode()==1){
            if(mIvPlay.getVisibility()==View.GONE){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }

    }

    private void initScreen() {
        if(!needOverride()){
            return;
        }
        if (instance.isPortrait()) {
            // 切换成竖屏
            backToNormal();
        } else {
            // 切换成横屏
            if (videoView!=null &&videoView.mVodController!=null&&videoView.getPlayMode()==1) {
                if (mIvPlay.getVisibility()==View.GONE) {
                    fullScreenPlay();
                }
            }
        }
    }

    public void fullScreenPlay(){
        if(videoView!=null&&videoView.mVodController!=null) {
            //横屏
            videoView.mVodControllerSmall.postDelayed(new Runnable() {
                @Override
                public void run() {
                    videoView.mVodController.onRequestPlayMode(2);
                }
            }, 100);
        }
    }

    public void backToNormal(){
        if(videoView!=null&&videoView.mVodController!=null&&videoView.getPlayMode()==2) {
            //横屏
            videoView.requestPlayMode(1);
            if(!instance.isPortrait){
                instance.toggleScreen();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreen();
    }


    /**
     * 全屏旋转播放逻辑配置结束**********************************************************************************
     */


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
                    SuperPlayerModel superPlayerModel=new SuperPlayerModel();
                    superPlayerModel.url="https://youku.cdn7-okzy.com/20200313/17725_bd26d414/index.m3u8";
                    superPlayerModel.title="";
                    videoView.playWithModel(superPlayerModel);
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
                SuperPlayerModel superPlayerModel=new SuperPlayerModel();
                superPlayerModel.url="https://youku.cdn7-okzy.com/20200313/17725_bd26d414/index.m3u8";
                superPlayerModel.title="";
                videoView.playWithModel(superPlayerModel);
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
