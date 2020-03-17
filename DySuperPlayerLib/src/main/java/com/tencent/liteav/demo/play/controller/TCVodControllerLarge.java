package com.tencent.liteav.demo.play.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.play.MySuperVideoView;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.R.drawable;
import com.tencent.liteav.demo.play.R.id;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.net.LogReport;
import com.tencent.liteav.demo.play.utils.TCTimeUtils;
import com.tencent.liteav.demo.play.view.DYLoadingView;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCPointSeekBar.OnSeekBarPointClickListener;
import com.tencent.liteav.demo.play.view.TCPointSeekBar.PointParams;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.liteav.demo.play.view.TCVodMoreView;
import com.tencent.liteav.demo.play.view.TCVodQualityView;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;
import com.tencent.rtmp.TXImageSprite;
import com.tencent.rtmp.TXLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TCVodControllerLarge extends TCVodControllerBase implements OnClickListener, TCVodMoreView.Callback, TCVodQualityView.Callback, OnSeekBarPointClickListener {
    private static final String TAG = "TCVodControllerLarge";
    private RelativeLayout mLayoutTop;
    private LinearLayout mLayoutBottom;
    private Context mContext;
    private ImageView mIvBack;
    private ImageView mIvPause;
    private TextView mTvQuality;
    private TextView mTvTitle;
    private ImageView mIvDanmuku;
    private ImageView mIvSnapshot;
    private ImageView mIvLock;
    private ImageView mIvMore;
    private TCVodQualityView mVodQualityView;
    private TCVodMoreView mVodMoreView;
    private boolean mDanmukuOn;
    private TextView mTvBackToLive;
    private TXImageSprite mTXImageSprite;
    private List<TCPlayKeyFrameDescInfo> mTXPlayKeyFrameDescInfos;
    private TextView mTvVttText;
    private int mSelectedPos = -1;
    private TCVodControllerLarge.HideLockViewRunnable mHideLockViewRunnable;
    private ImageView mIvWatermark;
    private ImageView ivFullScreen;
    private View mProgressStausView;
    private TextView mTvProgressStaus;
    private DYLoadingView dyLoading;

    public TCVodControllerLarge(Context context) {
        super(context);
        this.initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initViews(context);
    }

    void onShow() {
        this.mLayoutTop.setVisibility(VISIBLE);
        this.mLayoutBottom.setVisibility(VISIBLE);
        if ( this.getHandler()!=null&&this.mHideLockViewRunnable != null) {
            this.getHandler().removeCallbacks(this.mHideLockViewRunnable);
        }

        this.mIvLock.setVisibility(VISIBLE);
        if (this.mPlayType == 3 && this.mLayoutBottom.getVisibility() == VISIBLE) {
            this.mTvBackToLive.setVisibility(VISIBLE);
        }

    }

    void onHide() {
        this.mLayoutTop.setVisibility(GONE);
        this.mLayoutBottom.setVisibility(GONE);
        this.mVodQualityView.setVisibility(GONE);
        this.mTvVttText.setVisibility(GONE);
        this.mIvLock.setVisibility(GONE);
        if (this.mPlayType == 3) {
            this.mTvBackToLive.setVisibility(GONE);
        }

    }

    public void showLoadingView(boolean isShow){
        if(isShow){
            mProgressStausView.setVisibility(VISIBLE);
        }else{
            mProgressStausView.setVisibility(GONE);
        }
    }

    private void initViews(Context context) {
        this.mHideLockViewRunnable = new TCVodControllerLarge.HideLockViewRunnable(this);
        this.mContext = context;
        this.mLayoutInflater.inflate(R.layout.vod_controller_large, this);
        this.dyLoading=findViewById(id.dy_loading);
        this.dyLoading.setScales(0.8f, 1.5f);
        this.dyLoading.setDuration(400, 80);
        this.dyLoading.setRadius(15f, 15f, 10f);
        this.dyLoading.start();
        this.mProgressStausView=findViewById(id.ll_progress_wait_view);
        this.mTvProgressStaus = (TextView)this.findViewById(id.tv_progress_status);
        this.mLayoutTop = (RelativeLayout)this.findViewById(R.id.layout_top);
        this.ivFullScreen = (ImageView)this.findViewById(R.id.iv_fullscreen);
        this.mLayoutTop.setOnClickListener(this);
        this.mLayoutBottom = (LinearLayout)this.findViewById(R.id.layout_bottom);
        this.mLayoutBottom.setOnClickListener(this);
        this.mLayoutReplay = (LinearLayout)this.findViewById(R.id.layout_replay);
        this.mIvBack = (ImageView)this.findViewById(R.id.iv_back);
        this.mIvLock = (ImageView)this.findViewById(R.id.iv_lock);
        this.mTvTitle = (TextView)this.findViewById(R.id.tv_title);
        this.mIvPause = (ImageView)this.findViewById(R.id.iv_pause);
        this.mIvDanmuku = (ImageView)this.findViewById(R.id.iv_danmuku);
        this.mIvMore = (ImageView)this.findViewById(R.id.iv_more);
        this.mIvSnapshot = (ImageView)this.findViewById(R.id.iv_snapshot);
        this.mTvCurrent = (TextView)this.findViewById(R.id.tv_current);
        this.mTvDuration = (TextView)this.findViewById(R.id.tv_duration);
        this.mSeekBarProgress = (TCPointSeekBar)this.findViewById(R.id.seekbar_progress);
        this.mSeekBarProgress.setProgress(0);
        this.mSeekBarProgress.setOnPointClickListener(this);
        this.mSeekBarProgress.setOnSeekBarChangeListener(this);
        this.mTvQuality = (TextView)this.findViewById(R.id.tv_quality);
        this.mTvBackToLive = (TextView)this.findViewById(R.id.tv_backToLive);
        this.mPbLiveLoading = (ProgressBar)this.findViewById(R.id.pb_live);
        this.mVodQualityView = (TCVodQualityView)this.findViewById(R.id.vodQualityView);
        this.mVodQualityView.setCallback(this);
        this.mVodMoreView = (TCVodMoreView)this.findViewById(R.id.vodMoreView);
        this.mVodMoreView.setCallback(this);
        this.mTvBackToLive.setOnClickListener(this);
        this.mLayoutReplay.setOnClickListener(this);
        this.mIvLock.setOnClickListener(this);
        this.mIvBack.setOnClickListener(this);
        this.mIvPause.setOnClickListener(this);
        this.mIvDanmuku.setOnClickListener(this);
        this.mIvSnapshot.setOnClickListener(this);
        this.mIvMore.setOnClickListener(this);
        this.mTvQuality.setOnClickListener(this);
        this.ivFullScreen.setOnClickListener(this);
        this.mTvVttText = (TextView)this.findViewById(R.id.large_tv_vtt_text);
        this.mTvVttText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                float time = TCVodControllerLarge.this.mTXPlayKeyFrameDescInfos != null ? ((TCPlayKeyFrameDescInfo)TCVodControllerLarge.this.mTXPlayKeyFrameDescInfos.get(TCVodControllerLarge.this.mSelectedPos)).time : 0.0F;
                TCVodControllerLarge.this.mVodController.seekTo((int)time);
                TCVodControllerLarge.this.mVodController.resume();
                TCVodControllerLarge.this.mTvVttText.setVisibility(GONE);
                TCVodControllerLarge.this.updateReplay(false);
            }
        });
        if (this.mDefaultVideoQuality != null) {
            this.mTvQuality.setText(this.mDefaultVideoQuality.title);
        }

        this.mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout)this.findViewById(id.gesture_progress);
        this.mGestureVideoProgressLayout = (TCVideoProgressLayout)this.findViewById(id.video_progress_layout);
    }

    public void showAnimation(){
        if(dyLoading!=null){
            dyLoading.start();
        }
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i != id.iv_back && i != id.tv_title&&i!= id.iv_fullscreen) {
            if (i == id.iv_pause) {
                this.changePlayState();
            } else if (i == id.iv_danmuku) {
                this.toggleDanmu();
            } else if (i == id.iv_snapshot) {
                this.mVodController.onSnapshot();
            } else if (i == id.iv_more) {
                this.showMoreView();
            } else if (i == id.tv_quality) {
                this.showQualityView();
            } else if (i == id.iv_lock) {
                this.changeLockState();
            } else if (i == id.layout_replay) {
                this.replay();
            } else if (i == id.tv_backToLive) {
                this.backToLive();
            }
        } else {
            this.mVodController.onBackPress(2);
        }

    }

    private void backToLive() {
        this.mVodController.resumeLive();
    }

    private void changeLockState() {
        this.mLockScreen = !this.mLockScreen;
        this.mIvLock.setVisibility(VISIBLE);
        if (this.mHideLockViewRunnable != null) {
            this.getHandler().removeCallbacks(this.mHideLockViewRunnable);
            this.getHandler().postDelayed(this.mHideLockViewRunnable, 7000L);
        }

        if (this.mLockScreen) {
            this.mIvLock.setImageResource(drawable.ic_player_lock);
            this.hide();
            this.mIvLock.setVisibility(VISIBLE);
        } else {
            this.mIvLock.setImageResource(drawable.ic_player_unlock);
            this.show();
        }

    }

    private void toggleDanmu() {
        this.mDanmukuOn = !this.mDanmukuOn;
        if (this.mDanmukuOn) {
            this.mIvDanmuku.setImageResource(drawable.ic_danmuku_on);
        } else {
            this.mIvDanmuku.setImageResource(drawable.ic_danmuku_off);
        }

        this.mVodController.onDanmuku(this.mDanmukuOn);
    }

    private void showMoreView() {
        this.hide();
        this.mVodMoreView.setVisibility(VISIBLE);
    }

    private void showQualityView() {
        if (this.mVideoQualityList != null && this.mVideoQualityList.size() != 0) {
            this.mVodQualityView.setVisibility(VISIBLE);
            if (!this.mFirstShowQuality && this.mDefaultVideoQuality != null) {
                for(int i = 0; i < this.mVideoQualityList.size(); ++i) {
                    TCVideoQulity quality = (TCVideoQulity)this.mVideoQualityList.get(i);
                    if (quality != null && quality.title != null && quality.title.equals(this.mDefaultVideoQuality.title)) {
                        this.mVodQualityView.setDefaultSelectedQuality(i);
                        break;
                    }
                }

                this.mFirstShowQuality = true;
            }

            this.mVodQualityView.setVideoQualityList(this.mVideoQualityList);
        } else {
            TXLog.i("TCVodControllerLarge", "showQualityView mVideoQualityList null");
        }
    }

    public void updateVideoQulity(TCVideoQulity videoQulity) {
        this.mDefaultVideoQuality = videoQulity;
        if (this.mTvQuality != null) {
            this.mTvQuality.setText(videoQulity.title);
        }

        if (this.mVideoQualityList != null && this.mVideoQualityList.size() != 0) {
            for(int i = 0; i < this.mVideoQualityList.size(); ++i) {
                TCVideoQulity quality = (TCVideoQulity)this.mVideoQualityList.get(i);
                if (quality != null && quality.title != null && quality.title.equals(this.mDefaultVideoQuality.title)) {
                    this.mVodQualityView.setDefaultSelectedQuality(i);
                    break;
                }
            }
        }

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

    public void updateVttAndImages(TCPlayImageSpriteInfo info) {
        if (this.mTXImageSprite != null) {
            this.releaseTXImageSprite();
        }

        this.mGestureVideoProgressLayout.setProgressVisibility(info == null || info.imageUrls == null || info.imageUrls.size() == 0);
        if (this.mPlayType == 1) {
            this.mTXImageSprite = new TXImageSprite(this.getContext());
            if (info != null) {
                LogReport.getInstance().uploadLogs("image_sprite", 0L, 0);
                this.mTXImageSprite.setVTTUrlAndImageUrls(info.webVttUrl, info.imageUrls);
            } else {
                this.mTXImageSprite.setVTTUrlAndImageUrls((String)null, (List)null);
            }
        }

    }

    public void updateKeyFrameDescInfos(List<TCPlayKeyFrameDescInfo> list) {
        this.mTXPlayKeyFrameDescInfos = list;
    }

    public void release() {
        super.release();
        this.releaseTXImageSprite();
    }

    protected void finalize() throws Throwable {
        super.finalize();

        try {
            this.release();
        } catch (Exception var2) {
        } catch (Error var3) {
        }

    }

    private void releaseTXImageSprite() {
        if (this.mTXImageSprite != null) {
            Log.i("TCVodControllerLarge", "releaseTXImageSprite: release");
            this.mTXImageSprite.release();
            this.mTXImageSprite = null;
        }

    }

    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        super.onProgressChanged(seekBar, progress, isFromUser);
        if (isFromUser && this.mPlayType == 1) {
            this.setThumbnail(progress);
        }

    }

    protected void onGestureVideoProgress(int progress) {
        super.onGestureVideoProgress(progress);
        this.setThumbnail(progress);
    }

    private void setThumbnail(int progress) {
        float percentage = (float)progress / (float)this.mSeekBarProgress.getMax();
        float seekTime = this.mVodController.getDuration() * percentage;
        if (this.mTXImageSprite != null) {
            Bitmap bitmap = this.mTXImageSprite.getThumbnail(seekTime);
            if (bitmap != null) {
                this.mGestureVideoProgressLayout.setThumbnail(bitmap);
            }
        }

    }

    public void onSeekBarPointClick(final View view, final int pos) {
        if (this.mHideLockViewRunnable != null) {
            this.getHandler().removeCallbacks(this.mHideViewRunnable);
            this.getHandler().postDelayed(this.mHideViewRunnable, 7000L);
        }

        if (this.mTXPlayKeyFrameDescInfos != null) {
            LogReport.getInstance().uploadLogs("player_point", 0L, 0);
            this.mSelectedPos = pos;
            view.post(new Runnable() {
                public void run() {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    int viewX = location[0];
                    TCPlayKeyFrameDescInfo info = (TCPlayKeyFrameDescInfo)TCVodControllerLarge.this.mTXPlayKeyFrameDescInfos.get(pos);
                    String content = info.content;
                    TCVodControllerLarge.this.mTvVttText.setText(TCTimeUtils.formattedTime((long)info.time) + " " + content);
                    TCVodControllerLarge.this.mTvVttText.setVisibility(VISIBLE);
                    TCVodControllerLarge.this.adjustVttTextViewPos(viewX);
                }
            });
        }

    }

    private void adjustVttTextViewPos(final int viewX) {
        this.mTvVttText.post(new Runnable() {
            public void run() {
                int width = TCVodControllerLarge.this.mTvVttText.getWidth();
                int marginLeft = viewX - width / 2;
                LayoutParams params = (LayoutParams)TCVodControllerLarge.this.mTvVttText.getLayoutParams();
                params.leftMargin = marginLeft;
                if (marginLeft < 0) {
                    params.leftMargin = 0;
                }

                if (marginLeft + width > TCVodControllerLarge.this.getScreenWidth()) {
                    params.leftMargin = TCVodControllerLarge.this.getScreenWidth() - width;
                }

                TCVodControllerLarge.this.mTvVttText.setLayoutParams(params);
            }
        });
    }

    private int getScreenWidth() {
        return this.getResources().getDisplayMetrics().widthPixels;
    }

    public void updatePlayType(int playType) {
        super.updatePlayType(playType);
        switch(playType) {
            case 1:
                this.mTvBackToLive.setVisibility(GONE);
                this.mVodMoreView.updatePlayType(1);
                this.mTvDuration.setVisibility(VISIBLE);
                break;
            case 2:
                this.mTvBackToLive.setVisibility(GONE);
                this.mTvDuration.setVisibility(GONE);
                this.mVodMoreView.updatePlayType(2);
                this.mSeekBarProgress.setProgress(100);
                break;
            case 3:
                if (this.mLayoutBottom.getVisibility() == GONE) {
                    this.mTvBackToLive.setVisibility(VISIBLE);
                }

                this.mTvDuration.setVisibility(GONE);
                this.mVodMoreView.updatePlayType(3);
        }

    }

    public void show() {
        super.show();
        List<PointParams> pointParams = new ArrayList();
        if (this.mTXPlayKeyFrameDescInfos != null) {
            Iterator var2 = this.mTXPlayKeyFrameDescInfos.iterator();

            while(var2.hasNext()) {
                TCPlayKeyFrameDescInfo info = (TCPlayKeyFrameDescInfo)var2.next();
                int progress = (int)(info.time / this.mVodController.getDuration() * (float)this.mSeekBarProgress.getMax());
                pointParams.add(new PointParams(progress, -1));
            }
        }

        this.mSeekBarProgress.setPointList(pointParams);
    }

    public void onQualitySelect(TCVideoQulity quality) {
        this.mVodController.onQualitySelect(quality);
        this.mVodQualityView.setVisibility(GONE);
    }

    public void onSpeedChange(float speedLevel) {
        this.mVodController.onSpeedChange(speedLevel);
    }

    public void onMirrorChange(boolean isMirror) {
        this.mVodController.onMirrorChange(isMirror);
    }

    public void onHWAcceleration(boolean isAccelerate) {
        this.mVodController.onHWAcceleration(isAccelerate);
    }

    protected void onToggleControllerView() {
        super.onToggleControllerView();
        if (this.mLockScreen) {
            this.mIvLock.setVisibility(VISIBLE);
            if (this.mHideLockViewRunnable != null) {
                this.getHandler().removeCallbacks(this.mHideLockViewRunnable);
                this.getHandler().postDelayed(this.mHideLockViewRunnable, 7000L);
            }
        }

        if (this.mVodMoreView.getVisibility() == VISIBLE) {
            this.mVodMoreView.setVisibility(GONE);
        }

    }

    public void setWaterMarkBmp(Bitmap bmp, float xR, float yR) {
        super.setWaterMarkBmp(bmp, xR, yR);
    }

    private static class HideLockViewRunnable implements Runnable {
        private WeakReference<TCVodControllerLarge> mWefControllerLarge;

        public HideLockViewRunnable(TCVodControllerLarge controller) {
            this.mWefControllerLarge = new WeakReference(controller);
        }

        public void run() {
            if (this.mWefControllerLarge != null && this.mWefControllerLarge.get() != null) {
                ((TCVodControllerLarge)this.mWefControllerLarge.get()).mIvLock.setVisibility(GONE);
            }

        }
    }
}
