package com.tencent.liteav.demo.play.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.R.drawable;
import com.tencent.liteav.demo.play.R.id;
import com.tencent.liteav.demo.play.R.layout;
import com.tencent.liteav.demo.play.utils.VoiceBroadcastReceiver;
import com.tencent.liteav.demo.play.view.BeiSuPop;
import com.tencent.liteav.demo.play.view.DYLoadingView;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;


public class TCVodControllerSmall extends TCVodControllerBase implements OnClickListener{
    private static final String TAG = "TCVodControllerSmall";
    private LinearLayout mLayoutTop;
    private LinearLayout mLayoutBottom;
    private ImageView mIvPause;
    private ImageView mIvFullScreen;
    private TextView mTvTitle;
    private TextView mTvBackToLive;
    private ImageView mBackground;
    private Bitmap mBackgroundBmp;
    private ImageView mIvWatermark;
    private View mProgressStausView;
    private TextView mTvProgressStaus;
    private DYLoadingView dyLoading;
    private ImageView mIvVoice;
    public TextView mTvSpeed;

    public TCVodControllerSmall(Context context) {
        super(context);
        this.initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initViews();
    }

    void onShow() {
        this.mIvVoice.setVisibility(VISIBLE);
        this.mLayoutTop.setVisibility(VISIBLE);
        this.mLayoutBottom.setVisibility(VISIBLE);
        if (this.mPlayType == 3) {
            this.mTvBackToLive.setVisibility(VISIBLE);
        }

    }

    void onHide() {
        this.mLayoutTop.setVisibility(GONE);
        this.mIvVoice.setVisibility(GONE);
        if(beiSuPop!=null&&beiSuPop.popupWindow!=null&&beiSuPop.popupWindow.isShowing()){
            beiSuPop.dismissPop();
        }
        this.mLayoutBottom.setVisibility(GONE);
        if (this.mPlayType == 3) {
            this.mTvBackToLive.setVisibility(GONE);
        }

    }

    public void refreshVoiceStatus(){
        checkIsNoVoice(mIvVoice);
    }

    private void initViews() {
        this.mLayoutInflater.inflate(layout.vod_controller_small, this);
        this.mLayoutTop = (LinearLayout)this.findViewById(id.layout_top);
        this.mProgressStausView=findViewById(id.ll_progress_wait_view);
        this.dyLoading=findViewById(id.dy_loading);
        this.dyLoading.setScales(0.8f, 1.5f);
        this.dyLoading.setDuration(400, 80);
        this.dyLoading.setRadius(15f, 15f, 10f);
        this.dyLoading.start();
        this.mTvProgressStaus = (TextView)this.findViewById(id.tv_progress_status);
        this.mLayoutTop.setOnClickListener(this);
        this.mLayoutBottom = (LinearLayout)this.findViewById(id.layout_bottom);
        this.mLayoutBottom.setOnClickListener(this);
        this.mLayoutReplay = (LinearLayout)this.findViewById(id.layout_replay);
        this.mTvTitle = (TextView)this.findViewById(id.tv_title);
        this.mIvPause = (ImageView)this.findViewById(id.iv_pause);
        this.mTvCurrent = (TextView)this.findViewById(id.tv_current);
        this.mTvDuration = (TextView)this.findViewById(id.tv_duration);
        this.mTvSpeed=findViewById(id.tv_speed);
        this.mTvSpeed.setOnClickListener(this);
        this.mIvVoice=this.findViewById(id.iv_voice);
        this.mIvVoice.setOnClickListener(this);
        this.mSeekBarProgress = (TCPointSeekBar)this.findViewById(id.seekbar_progress);
        this.mSeekBarProgress.setProgress(0);
        this.mSeekBarProgress.setMax(100);
        this.mIvFullScreen = (ImageView)this.findViewById(id.iv_fullscreen);
        this.mTvBackToLive = (TextView)this.findViewById(id.tv_backToLive);
        this.mPbLiveLoading = (ProgressBar)this.findViewById(id.pb_live);
        this.mTvBackToLive.setOnClickListener(this);
        this.mIvPause.setOnClickListener(this);
        this.mIvFullScreen.setOnClickListener(this);
        this.mLayoutTop.setOnClickListener(this);
        this.mLayoutReplay.setOnClickListener(this);
        this.mSeekBarProgress.setOnSeekBarChangeListener(this);
        this.mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout)this.findViewById(id.gesture_progress);
        this.mGestureVideoProgressLayout = (TCVideoProgressLayout)this.findViewById(id.video_progress_layout);
        this.mBackground = (ImageView)this.findViewById(id.small_iv_background);
        this.setBackground(this.mBackgroundBmp);
        this.mIvWatermark = (ImageView)this.findViewById(id.small_iv_water_mark);
        checkIsNoVoice(mIvVoice);
    }




    public void showLoadingView(boolean isShow){
        if(isShow){
            updatePlayState(true);
            mProgressStausView.setVisibility(VISIBLE);
        }else{
            mProgressStausView.setVisibility(GONE);
        }
    }

    public void setBackground(final Bitmap bitmap) {
        this.post(new Runnable() {
            public void run() {
                if (bitmap != null) {
                    if (TCVodControllerSmall.this.mBackground == null) {
                        TCVodControllerSmall.this.mBackgroundBmp = bitmap;
                    } else {
                        TCVodControllerSmall.this.setBitmap(TCVodControllerSmall.this.mBackground, TCVodControllerSmall.this.mBackgroundBmp);
                    }

                }
            }
        });
    }

    public void dismissBackground() {
        this.post(new Runnable() {
            public void run() {
                if (TCVodControllerSmall.this.mBackground.getVisibility() == VISIBLE) {
                    ValueAnimator alpha = ValueAnimator.ofFloat(new float[]{1.0F, 0.0F});
                    alpha.setDuration(500L);
                    alpha.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (Float)animation.getAnimatedValue();
                            TCVodControllerSmall.this.mBackground.setAlpha(value);
                            if (value == 0.0F) {
                                TCVodControllerSmall.this.mBackground.setVisibility(GONE);
                            }

                        }
                    });
                    alpha.start();
                }
            }
        });
    }

    public void showBackground() {
        this.post(new Runnable() {
            public void run() {
                ValueAnimator alpha = ValueAnimator.ofFloat(new float[]{0.0F, 1.0F});
                alpha.setDuration(500L);
                alpha.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float)animation.getAnimatedValue();
                        TCVodControllerSmall.this.mBackground.setAlpha(value);
                        if (value == 1.0F) {
                            TCVodControllerSmall.this.mBackground.setVisibility(VISIBLE);
                        }

                    }
                });
                alpha.start();
            }
        });
    }

    public BeiSuPop.OnItemClickListener onRateCheckedListener;

    public void setOnRateCheckedListener(BeiSuPop.OnItemClickListener onRateCheckedListener) {
        this.onRateCheckedListener = onRateCheckedListener;
    }




    public void onClick(View view) {
        int i = view.getId();
        if (i == id.layout_top) {
            this.onBack();
        } else if (i == id.iv_pause) {
            this.changePlayState();
        } else if (i == id.iv_fullscreen) {
            isClickFullScreen=true;
            this.fullScreen();
        } else if (i == id.layout_replay) {
            this.replay();
        } else if (i == id.tv_backToLive) {
            this.backToLive();
        }else if(i==id.iv_voice){
            switchVoice(getContext(),this.mIvVoice);
        }else if(i==id.tv_speed){
            showSpeedPop((Activity) getContext(), mTvSpeed,onRateCheckedListener,false);
        }

    }

    private void backToLive() {
        this.mVodController.resumeLive();
    }

    public void onBack() {
        this.mVodController.onBackPress(1);
    }

    public void fullScreen() {
        this.mVodController.onRequestPlayMode(2);
    }

    public void updatePlayState(boolean isStart) {
        if (isStart) {
            this.mIvPause.setImageResource(drawable.ic_vod_pause_normal);
        } else {
            this.mIvPause.setImageResource(drawable.ic_vod_play_normal);
        }

    }

    public void updateTitle(String title) {
        super.updateTitle(title);
        this.mTvTitle.setText(this.mTitle);
    }

    public void updatePlayType(int playType) {
        TXCLog.i("TCVodControllerSmall", "updatePlayType playType:" + playType);
        super.updatePlayType(playType);
        switch(playType) {
            case 1:
                this.mTvBackToLive.setVisibility(GONE);
                this.mTvDuration.setVisibility(VISIBLE);
                break;
            case 2:
                this.mTvBackToLive.setVisibility(GONE);
                this.mTvDuration.setVisibility(GONE);
                this.mSeekBarProgress.setProgress(100);
                break;
            case 3:
                if (this.mLayoutBottom.getVisibility() == VISIBLE) {
                    this.mTvBackToLive.setVisibility(VISIBLE);
                }

                this.mTvDuration.setVisibility(GONE);
        }

    }

    public void showAnimation(){
        if(dyLoading!=null){
            dyLoading.start();
        }
    }

    public void setWaterMarkBmp(final Bitmap bmp, final float xR, final float yR) {
        super.setWaterMarkBmp(bmp, xR, yR);
        if (bmp != null) {
            this.post(new Runnable() {
                public void run() {
                    int width = TCVodControllerSmall.this.getWidth();
                    int height = TCVodControllerSmall.this.getHeight();
                    int x = (int)((float)width * xR) - bmp.getWidth() / 2;
                    int y = (int)((float)height * yR) - bmp.getHeight() / 2;
                    TCVodControllerSmall.this.mIvWatermark.setX((float)x);
                    TCVodControllerSmall.this.mIvWatermark.setY((float)y);
                    TCVodControllerSmall.this.mIvWatermark.setVisibility(VISIBLE);
                    TCVodControllerSmall.this.setBitmap(TCVodControllerSmall.this.mIvWatermark, bmp);
                }
            });
        } else {
            this.mIvWatermark.setVisibility(GONE);
        }

    }
}
