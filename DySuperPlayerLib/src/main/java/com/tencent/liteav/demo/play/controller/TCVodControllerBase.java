package com.tencent.liteav.demo.play.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R.drawable;
import com.tencent.liteav.demo.play.utils.TCTimeUtils;
import com.tencent.liteav.demo.play.utils.VideoGestureUtil;
import com.tencent.liteav.demo.play.utils.VideoGestureUtil.VideoGestureListener;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCPointSeekBar.OnSeekBarChangeListener;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class TCVodControllerBase extends RelativeLayout implements OnSeekBarChangeListener {
    private static final int MAX_SHIFT_TIME = 7200;
    private static final String TAG = "TCVodControllerBase";
    protected LayoutInflater mLayoutInflater;
    protected TCVodControllerBase.VodController mVodController;
    protected GestureDetector mGestureDetector;
    private boolean isShowing;
    protected boolean mLockScreen;
    private static final double RADIUS_SLOP = 0.7853981633974483D;
    protected TCVideoQulity mDefaultVideoQuality;
    protected ArrayList<TCVideoQulity> mVideoQualityList;
    protected int mPlayType;
    protected long mLivePushDuration;
    protected String mTitle;
    protected TextView mTvCurrent;
    protected TextView mTvDuration;
    protected TCPointSeekBar mSeekBarProgress;
    protected LinearLayout mLayoutReplay;
    protected ProgressBar mPbLiveLoading;
    protected VideoGestureUtil mVideoGestureUtil;
    protected TCVolumeBrightnessProgressLayout mGestureVolumeBrightnessProgressLayout;
    protected TCVideoProgressLayout mGestureVideoProgressLayout;
    protected TCVodControllerBase.HideViewControllerViewRunnable mHideViewRunnable;
    protected boolean mIsChangingSeekBarProgress;
    protected boolean mFirstShowQuality;
    protected Bitmap mWaterMarkBmp;
    protected float mWaterMarkBmpX;
    protected float mWaterMarkBmpY;

    public TCVodControllerBase(Context context) {
        super(context);
        this.init();
    }

    public TCVodControllerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public TCVodControllerBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        this.mHideViewRunnable = new TCVodControllerBase.HideViewControllerViewRunnable(this);
        this.mLayoutInflater = LayoutInflater.from(this.getContext());
        this.mGestureDetector = new GestureDetector(this.getContext(), new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                if (TCVodControllerBase.this.mLockScreen) {
                    return false;
                } else {
                    TCVodControllerBase.this.changePlayState();
                    TCVodControllerBase.this.show();
                    if (TCVodControllerBase.this.mHideViewRunnable != null) {
                        TCVodControllerBase.this.getHandler().removeCallbacks(TCVodControllerBase.this.mHideViewRunnable);
                        TCVodControllerBase.this.getHandler().postDelayed(TCVodControllerBase.this.mHideViewRunnable, 7000L);
                    }

                    return true;
                }
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                TCVodControllerBase.this.onToggleControllerView();
                return true;
            }

            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (TCVodControllerBase.this.mLockScreen) {
                    return false;
                } else if (downEvent != null && moveEvent != null) {
                    if (TCVodControllerBase.this.mVideoGestureUtil != null && TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout != null) {
                        TCVodControllerBase.this.mVideoGestureUtil.check(TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.getHeight(), downEvent, moveEvent, distanceX, distanceY);
                    }

                    return true;
                } else {
                    return false;
                }
            }

            public boolean onDown(MotionEvent e) {
                if (TCVodControllerBase.this.mLockScreen) {
                    return true;
                } else {
                    if (TCVodControllerBase.this.mVideoGestureUtil != null) {
                        TCVodControllerBase.this.mVideoGestureUtil.reset(TCVodControllerBase.this.getWidth(), TCVodControllerBase.this.mSeekBarProgress.getProgress());
                    }

                    return true;
                }
            }
        });
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mVideoGestureUtil = new VideoGestureUtil(this.getContext());
        this.mVideoGestureUtil.setVideoGestureListener(new VideoGestureListener() {
            public void onBrightnessGesture(float newBrightness) {
                if (TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout != null) {
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.setProgress((int)(newBrightness * 100.0F));
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.setImageResource(drawable.ic_light_max);
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.show();
                }

            }

            public void onVolumeGesture(float volumeProgress) {
                if (TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout != null) {
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.setImageResource(drawable.ic_volume_max);
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.setProgress((int)volumeProgress);
                    TCVodControllerBase.this.mGestureVolumeBrightnessProgressLayout.show();
                }

            }

            public void onSeekGesture(int progress) {
                TCVodControllerBase.this.mIsChangingSeekBarProgress = true;
                if (TCVodControllerBase.this.mGestureVideoProgressLayout != null) {
                    if (progress > TCVodControllerBase.this.mSeekBarProgress.getMax()) {
                        progress = TCVodControllerBase.this.mSeekBarProgress.getMax();
                    }

                    if (progress < 0) {
                        progress = 0;
                    }

                    TCVodControllerBase.this.mGestureVideoProgressLayout.setProgress(progress);
                    TCVodControllerBase.this.mGestureVideoProgressLayout.show();
                    float percentage = (float)progress / (float)TCVodControllerBase.this.mSeekBarProgress.getMax();
                    float currentTime = TCVodControllerBase.this.mVodController.getDuration() * percentage;
                    if (TCVodControllerBase.this.mPlayType != 2 && TCVodControllerBase.this.mPlayType != 3) {
                        TCVodControllerBase.this.mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long)currentTime) + " / " + TCTimeUtils.formattedTime((long)TCVodControllerBase.this.mVodController.getDuration()));
                    } else {
                        if (TCVodControllerBase.this.mLivePushDuration > 7200L) {
                            currentTime = (float)((int)((float)TCVodControllerBase.this.mLivePushDuration - 7200.0F * (1.0F - percentage)));
                        } else {
                            currentTime = (float)TCVodControllerBase.this.mLivePushDuration * percentage;
                        }

                        TCVodControllerBase.this.mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long)currentTime));
                    }

                    TCVodControllerBase.this.onGestureVideoProgress(progress);
                }

                if (TCVodControllerBase.this.mSeekBarProgress != null) {
                    TCVodControllerBase.this.mSeekBarProgress.setProgress(progress);
                }

            }
        });
    }

    public void setVideoQualityList(ArrayList<TCVideoQulity> videoQualityList) {
        this.mVideoQualityList = videoQualityList;
        this.mFirstShowQuality = false;
    }

    public void setWaterMarkBmp(Bitmap bmp, float x, float y) {
        this.mWaterMarkBmp = bmp;
        this.mWaterMarkBmpY = y;
        this.mWaterMarkBmpX = x;
    }

    public void updateTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            this.mTitle = title;
        } else {
            this.mTitle = "";
        }

    }

    public void updateVideoProgress(long current, long duration) {
        if (current < 0L) {
            current = 0L;
        }

        if (duration < 0L) {
            duration = 0L;
        }

        if (this.mTvCurrent != null) {
            this.mTvCurrent.setText(TCTimeUtils.formattedTime(current));
        }

        float percentage = duration > 0L ? (float)current / (float)duration : 1.0F;
        if (current == 0L) {
            this.mLivePushDuration = 0L;
            percentage = 0.0F;
        }

        if (this.mPlayType == 2 || this.mPlayType == 3) {
            this.mLivePushDuration = this.mLivePushDuration > current ? this.mLivePushDuration : current;
            long leftTime = duration - current;
            if (duration > 7200L) {
                duration = 7200L;
            }

            percentage = 1.0F - (float)leftTime / (float)duration;
        }

        if (percentage >= 0.0F && percentage <= 1.0F) {
            if (this.mSeekBarProgress != null) {
                int progress = Math.round(percentage * (float)this.mSeekBarProgress.getMax());
                if (!this.mIsChangingSeekBarProgress) {
                    this.mSeekBarProgress.setProgress(progress);
                }
            }

            if (this.mTvDuration != null) {
                this.mTvDuration.setText(TCTimeUtils.formattedTime(duration));
            }
        }

    }

    public void setVodController(TCVodControllerBase.VodController vodController) {
        this.mVodController = vodController;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGestureDetector != null) {
            this.mGestureDetector.onTouchEvent(event);
        }

        if (!this.mLockScreen && event.getAction() == 1 && this.mVideoGestureUtil != null && this.mVideoGestureUtil.isVideoProgressModel()) {
            int progress = this.mVideoGestureUtil.getVideoProgress();
            if (progress > this.mSeekBarProgress.getMax()) {
                progress = this.mSeekBarProgress.getMax();
            }

            if (progress < 0) {
                progress = 0;
            }

            this.mSeekBarProgress.setProgress(progress);
            int seekTime;
            float percentage = (float)progress * 1.0F / (float)this.mSeekBarProgress.getMax();
            if (this.mPlayType != 2 && this.mPlayType != 3) {
                seekTime = (int)(percentage * this.mVodController.getDuration());
            } else if (this.mLivePushDuration > 7200L) {
                seekTime = (int)((float)this.mLivePushDuration - 7200.0F * (1.0F - percentage));
            } else {
                seekTime = (int)((float)this.mLivePushDuration * percentage);
            }

            this.mVodController.seekTo(seekTime);
            this.mIsChangingSeekBarProgress = false;
        }

        if (event.getAction() == 0) {
            this.getHandler().removeCallbacks(this.mHideViewRunnable);
        } else if (event.getAction() == 1) {
            this.getHandler().postDelayed(this.mHideViewRunnable, 7000L);
        }

        return true;
    }

    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        if (this.mGestureVideoProgressLayout != null && isFromUser) {
            this.mGestureVideoProgressLayout.show();
            float percentage = (float)progress / (float)seekBar.getMax();
            float currentTime = this.mVodController.getDuration() * percentage;
            if (this.mPlayType != 2 && this.mPlayType != 3) {
                this.mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long)currentTime) + " / " + TCTimeUtils.formattedTime((long)this.mVodController.getDuration()));
            } else {
                if (this.mLivePushDuration > 7200L) {
                    currentTime = (float)((int)((float)this.mLivePushDuration - 7200.0F * (1.0F - percentage)));
                } else {
                    currentTime = (float)this.mLivePushDuration * percentage;
                }

                this.mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long)currentTime));
            }

            this.mGestureVideoProgressLayout.setProgress(progress);
        }

    }

    public void onStartTrackingTouch(TCPointSeekBar seekBar) {
        this.getHandler().removeCallbacks(this.mHideViewRunnable);
    }

    public void onStopTrackingTouch(TCPointSeekBar seekBar) {
        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();
        switch(this.mPlayType) {
            case 1:
                if (curProgress >= 0 && curProgress <= maxProgress) {
                    this.updateReplay(false);
                    float percentage = (float)curProgress / (float)maxProgress;
                    int position = (int)(this.mVodController.getDuration() * percentage);
                    this.mVodController.seekTo(position);
                    this.mVodController.resume();
                }
                break;
            case 2:
            case 3:
                this.updateLiveLoadingState(true);
                int seekTime = (int)((float)(this.mLivePushDuration * (long)curProgress) * 1.0F / (float)maxProgress);
                if (this.mLivePushDuration > 7200L) {
                    seekTime = (int)((float)this.mLivePushDuration - (float)(7200 * (maxProgress - curProgress)) * 1.0F / (float)maxProgress);
                }

                this.mVodController.seekTo(seekTime);
        }

        this.getHandler().postDelayed(this.mHideViewRunnable, 7000L);
    }

    public void updateReplay(boolean replay) {
        if (this.mLayoutReplay != null) {
            this.mLayoutReplay.setVisibility(replay ? VISIBLE : GONE);
        }

    }

    public void updateLiveLoadingState(boolean loading) {
        if (this.mPbLiveLoading != null) {
            this.mPbLiveLoading.setVisibility(loading ? VISIBLE : GONE);
        }

    }

    protected void replay() {
        this.updateReplay(false);
        this.mVodController.onReplay();
    }

    protected void changePlayState() {
        if (this.mVodController.isPlaying()) {
            this.mVodController.pause();
            this.show();
        } else if (!this.mVodController.isPlaying()) {
            this.updateReplay(false);
            this.mVodController.resume();
            this.show();
        }

    }

    protected void onToggleControllerView() {
        if (!this.mLockScreen) {
            if (this.isShowing) {
                this.hide();
            } else {
                this.show();
                if (this.mHideViewRunnable != null) {
                    this.getHandler().removeCallbacks(this.mHideViewRunnable);
                    this.getHandler().postDelayed(this.mHideViewRunnable, 7000L);
                }
            }
        }

    }

    public void show() {
        this.isShowing = true;
        this.onShow();
    }

    public void hide() {
        this.isShowing = false;
        this.onHide();
    }

    public void release() {
    }

    protected void setBitmap(ImageView view, Bitmap bitmap) {
        if (view != null && bitmap != null) {
            if (VERSION.SDK_INT >= 16) {
                view.setBackground(new BitmapDrawable(this.getContext().getResources(), bitmap));
            } else {
                view.setBackgroundDrawable(new BitmapDrawable(this.getContext().getResources(), bitmap));
            }

        }
    }

    abstract void onShow();

    abstract void onHide();

    public void updatePlayType(int playType) {
        this.mPlayType = playType;
    }

    protected void onGestureVideoProgress(int currentProgress) {
    }

    private static class HideViewControllerViewRunnable implements Runnable {
        public WeakReference<TCVodControllerBase> mWefControlBase;

        public HideViewControllerViewRunnable(TCVodControllerBase base) {
            this.mWefControlBase = new WeakReference(base);
        }

        public void run() {
            if (this.mWefControlBase != null && this.mWefControlBase.get() != null) {
                ((TCVodControllerBase)this.mWefControlBase.get()).hide();
            }

        }
    }

    public interface VodController {
        void onRequestPlayMode(int var1);

        void onBackPress(int var1);

        void resume();

        void pause();

        float getDuration();

        float getCurrentPlaybackTime();

        void seekTo(int var1);

        boolean isPlaying();

        void onDanmuku(boolean var1);

        void onSnapshot();

        void onQualitySelect(TCVideoQulity var1);

        void onSpeedChange(float var1);

        void onMirrorChange(boolean var1);

        void onHWAcceleration(boolean var1);

        void onFloatUpdate(int var1, int var2);

        void onReplay();

        void resumeLive();
    }
}
