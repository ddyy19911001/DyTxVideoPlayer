package com.tencent.liteav.demo.play;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;
import com.tencent.liteav.demo.play.controller.TCVodControllerBase;
import com.tencent.liteav.demo.play.controller.TCVodControllerFloat;
import com.tencent.liteav.demo.play.controller.TCVodControllerLarge;
import com.tencent.liteav.demo.play.controller.TCVodControllerSmall;
import com.tencent.liteav.demo.play.net.LogReport;
import com.tencent.liteav.demo.play.utils.NetWatcher;
import com.tencent.liteav.demo.play.utils.SuperPlayerUtil;
import com.tencent.liteav.demo.play.v3.SuperPlayerModelWrapper;
import com.tencent.liteav.demo.play.v3.SuperVodInfoLoaderV3;
import com.tencent.liteav.demo.play.view.BeiSuPop;
import com.tencent.liteav.demo.play.view.TCDanmuView;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.tencent.rtmp.TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
import static com.tencent.rtmp.TXLiveConstants.RENDER_ROTATION_PORTRAIT;

public class MySuperVideoView extends RelativeLayout implements ITXVodPlayListener, ITXLivePlayListener, BeiSuPop.OnItemClickListener {
    private static final String TAG = "MySuperVideoView";
    public static final int SMALL_CONTROLLER_MODE = 1;
    public static final int LARGE_CONTROLLER_MODE = 2;
    public static final int FLOAT_CONTROLLER_MODE = 3;
    private Context mContext;
    private int mPlayMode = 1;
    private boolean mLockScreen = false;
    private ViewGroup mRootView;
    private TXCloudVideoView mTXCloudVideoView;
    public TCVodControllerLarge mVodControllerLarge;
    public TCVodControllerSmall mVodControllerSmall;
    public TCVodControllerFloat mVodControllerFloat;
    private TCDanmuView mDanmuView;
    private RelativeLayout.LayoutParams mLayoutParamWindowMode;
    private LayoutParams mLayoutParamFullScreenMode;
    private LayoutParams mVodControllerSmallParams;
    private LayoutParams mVodControllerLargeParams;
    private TXVodPlayer mVodPlayer;
    private TXVodPlayConfig mVodPlayConfig;
    private TXLivePlayer mLivePlayer;
    private TXLivePlayConfig mLivePlayConfig;
    private OnSuperPlayerViewCallback mPlayerViewCallback;
    private int mCurrentPlayState = 1;
    private boolean mDefaultSet;
    private long mReportLiveStartTime = -1L;
    private long mReportVodStartTime = -1L;
    private int mCurrentPlayType;
    private NetWatcher mWatcher;
    private boolean mIsMultiBitrateStream;
    private boolean mIsPlayWithFileid;
    private String mCurrentPlayVideoURL;
    private SuperPlayerModelWrapper mCurrentModelWrapper;
    private Bitmap mWaterMarkBmp;
    private float mWaterMarkBmpX;
    private float mWaterMarkBmpY;
    private long mMaxLiveProgressTime;
    private float mCurrentTimeWhenPause;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private boolean mChangeHWAcceleration;
    private int mSeekPos;
    public TCVodControllerBase.VodController mVodController;
    private final int OP_SYSTEM_ALERT_WINDOW;
    private long notVipCanLookTimes=60*1000;

    public MySuperVideoView(Context context) {
        super(context);
        this.mVodController = new NamelessClass_1();
        this.OP_SYSTEM_ALERT_WINDOW = 24;
        this.initView(context);
    }

    public MySuperVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mVodController = new NamelessClass_1();
        this.OP_SYSTEM_ALERT_WINDOW = 24;
        this.initView(context);
    }

    public MySuperVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mVodController = new NamelessClass_1();
        this.OP_SYSTEM_ALERT_WINDOW = 24;
        this.initView(context);
    }

    public TCVodControllerBase.VodController getmVodController() {
        return mVodController;
    }

    public TXVodPlayer getmVodPlayer() {
        return mVodPlayer;
    }

    public void onResume() {
        if (this.mDanmuView != null && this.mDanmuView.isPrepared() && this.mDanmuView.isPaused()) {
            this.mDanmuView.resume();
        }
        this.resume();
    }

    public void onPause() {
        if (this.mDanmuView != null && this.mDanmuView.isPrepared()) {
            this.mDanmuView.pause();
        }
        if(this.mVodControllerLarge!=null){
            this.mVodControllerLarge.showLoadingView(false);
        }
        if(this.mVodControllerSmall!=null){
            this.mVodControllerSmall.showLoadingView(false);
        }
        this.pause();
    }


    public void setPlayerViewCallback(OnSuperPlayerViewCallback callback) {
        this.mPlayerViewCallback = callback;
    }

    public int getPlayMode() {
        return this.mPlayMode;
    }

    public int getPlayState() {
        return this.mCurrentPlayState;
    }

    public void resetPlayer() {
        if (this.mDanmuView != null) {
            this.mDanmuView.release();
            this.mDanmuView = null;
        }

        this.stopPlay();
    }

    public void showSmallCoverPic(final String coverUrl) {
        if (this.mTXCloudVideoView != null) {
            this.mTXCloudVideoView.post(new Runnable() {
                public void run() {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            Bitmap bmp = null;
                            InputStream in = null;

                            try {
                                URL imgUrl = new URL(coverUrl);
                                URLConnection con = imgUrl.openConnection();
                                in = con.getInputStream();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeStream(in, (Rect)null, options);
                                int sampleSize = MySuperVideoView.this.calculateInSampleSize(options.outWidth, options.outHeight, MySuperVideoView.this.mTXCloudVideoView.getWidth(), MySuperVideoView.this.mTXCloudVideoView.getHeight());
                                options = new BitmapFactory.Options();
                                options.inSampleSize = sampleSize;
                                options.inJustDecodeBounds = false;
                                in.close();
                                con = imgUrl.openConnection();
                                in = con.getInputStream();
                                bmp = BitmapFactory.decodeStream(in, (Rect)null, options);
                                in.close();
                                MySuperVideoView.this.mVodControllerSmall.setBackground(bmp);
                                MySuperVideoView.this.mVodControllerSmall.showBackground();
                            } catch (MalformedURLException var17) {
                                var17.printStackTrace();
                            } catch (IOException var18) {
                                var18.printStackTrace();
                            } finally {
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException var16) {
                                        var16.printStackTrace();
                                    }
                                }

                            }

                        }
                    });
                }
            });
        }
    }

    private void initView(Context context) {
        setKeepScreenOn(true);
        this.mContext = context;
        this.mRootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.super_vod_player_view, (ViewGroup)null);
        this.mTXCloudVideoView = this.mRootView.findViewById(R.id.cloud_video_view);
        this.mVodControllerLarge = this.mRootView.findViewById(R.id.controller_large);
        this.mVodControllerSmall = this.mRootView.findViewById(R.id.controller_small);
        this.mVodControllerFloat = this.mRootView.findViewById(R.id.controller_float);
        this.mDanmuView = (TCDanmuView)this.mRootView.findViewById(R.id.danmaku_view);
        this.mVodControllerSmall.setOnRateCheckedListener(this);
        this.mVodControllerLarge.setOnRateCheckedListener(this);
        this.mVodControllerSmallParams = new LayoutParams(-1, -1);
        this.mVodControllerLargeParams = new LayoutParams(-1, -1);
        this.mVodControllerLarge.setVodController(this.mVodController);
        this.mVodControllerSmall.setVodController(this.mVodController);
        this.mVodControllerFloat.setVodController(this.mVodController);
        this.mVodControllerLarge.setWaterMarkBmp(this.mWaterMarkBmp, this.mWaterMarkBmpX, this.mWaterMarkBmpY);
        this.mVodControllerSmall.setWaterMarkBmp(this.mWaterMarkBmp, this.mWaterMarkBmpX, this.mWaterMarkBmpY);
        this.removeAllViews();
        this.mRootView.removeView(this.mDanmuView);
        this.mRootView.removeView(this.mTXCloudVideoView);
        this.mRootView.removeView(this.mVodControllerSmall);
        this.mRootView.removeView(this.mVodControllerLarge);
        this.mRootView.removeView(this.mVodControllerFloat);
        this.addView(this.mTXCloudVideoView);
        if (this.mPlayMode == 2) {
            this.addView(this.mVodControllerLarge);
            this.mVodControllerLarge.show();
        } else if (this.mPlayMode == 1) {
            this.addView(this.mVodControllerSmall);
            this.mVodControllerSmall.show();
        }

        this.addView(this.mDanmuView);
        this.post(new Runnable() {
            public void run() {
                if (MySuperVideoView.this.mPlayMode == 1) {
                    mLayoutParamWindowMode = (LayoutParams) getLayoutParams();
                }
                try {
                    Class parentLayoutParamClazz = MySuperVideoView.this.getLayoutParams().getClass();
                    Constructor constructor = parentLayoutParamClazz.getDeclaredConstructor(Integer.TYPE, Integer.TYPE);
                    MySuperVideoView.this.mLayoutParamFullScreenMode = (LayoutParams)constructor.newInstance(-1, -1);
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        });
        LogReport.getInstance().setAppName(this.getApplicationName());
        LogReport.getInstance().setPackageName(this.getPackagename());
    }

    private String getApplicationName() {
        Context context = this.mContext.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private String getPackagename() {
        String packagename = "";
        if (this.mContext != null) {
            try {
                PackageInfo info = this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0);
                packagename = info.packageName;
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return packagename;
    }

    public void initVodPlayer(Context context) {
        if (this.mVodPlayer == null) {
            this.mVodPlayer = new TXVodPlayer(context);
            SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
            this.mVodPlayConfig = new TXVodPlayConfig();
            String path=context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/";
            File file=new File(path);
            if(!file.exists()){
                file.mkdirs();
            }
            this.mVodPlayConfig.setCacheFolderPath(file.getAbsolutePath());
            this.mVodPlayConfig.setMaxCacheItems(config.maxCacheItem);
            this.mVodPlayer.setConfig(this.mVodPlayConfig);
            config.renderMode=RENDER_MODE_FULL_FILL_SCREEN;
            this.mVodPlayer.setRenderMode(config.renderMode);
            this.mVodPlayer.stopPlay(true);
            this.mVodPlayer.enableHardwareDecode(true);
            this.mVodPlayer.setRenderRotation(RENDER_ROTATION_PORTRAIT);
            this.mVodPlayer.setVodListener(this);
            this.mVodPlayer.enableHardwareDecode(config.enableHWAcceleration);
        }
    }

    public void initLivePlayer(Context context) {
        if (this.mLivePlayer == null) {
            this.mLivePlayer = new TXLivePlayer(context);
            SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
            this.mLivePlayConfig = new TXLivePlayConfig();
            this.mLivePlayer.setConfig(this.mLivePlayConfig);
            config.renderMode=RENDER_MODE_FULL_FILL_SCREEN;
            this.mLivePlayer.setRenderMode(config.renderMode);
            this.mLivePlayer.setRenderRotation(RENDER_ROTATION_PORTRAIT);
            this.mLivePlayer.setPlayListener(this);
            this.mLivePlayer.enableHardwareDecode(config.enableHWAcceleration);
        }
    }

    public void playWithModel(final SuperPlayerModel modelV3) {
        this.mVodControllerLarge.updateVttAndImages((TCPlayImageSpriteInfo)null);
        this.mVodControllerLarge.updateKeyFrameDescInfos((List)null);
        this.mCurrentTimeWhenPause = 0.0F;
        SuperPlayerModelWrapper modelWrapper = new SuperPlayerModelWrapper(modelV3);
        this.mCurrentModelWrapper = modelWrapper;
        if (modelV3.videoId != null) {
            SuperVodInfoLoaderV3 v3Loader = new SuperVodInfoLoaderV3();
            v3Loader.getVodByFileId(modelWrapper, new SuperVodInfoLoaderV3.OnVodInfoLoadListener() {
                public void onSuccess(SuperPlayerModelWrapper model) {
                    Log.i("SuperPlayerView", "onSuccess: requestModel = " + model.toString());
                    MySuperVideoView.this.stopPlay();
                    MySuperVideoView.this.initLivePlayer(MySuperVideoView.this.getContext());
                    MySuperVideoView.this.initVodPlayer(MySuperVideoView.this.getContext());
                    MySuperVideoView.this.mReportVodStartTime = System.currentTimeMillis();
                    MySuperVideoView.this.mVodPlayer.setPlayerView(mTXCloudVideoView);
                    MySuperVideoView.this.playV3ModelVideo(model);
                    MySuperVideoView.this.playV2ModelVideo(model);
                    MySuperVideoView.this.mCurrentPlayType = 1;
                    MySuperVideoView.this.mVodControllerSmall.updatePlayType(1);
                    MySuperVideoView.this.mVodControllerLarge.updatePlayType(1);
                    String title = !TextUtils.isEmpty(modelV3.title) ? modelV3.title : (model.videoInfo != null && !TextUtils.isEmpty(model.videoInfo.videoName) ? model.videoInfo.videoName : "");
                    MySuperVideoView.this.mVodControllerSmall.updateTitle(title);
                    MySuperVideoView.this.mVodControllerLarge.updateTitle(title);
                    MySuperVideoView.this.mVodControllerSmall.updateVideoProgress(0L, 0L);
                    MySuperVideoView.this.mVodControllerLarge.updateVideoProgress(0L, 0L);
                    MySuperVideoView.this.mVodControllerLarge.updateVttAndImages(model.imageInfo);
                    MySuperVideoView.this.mVodControllerLarge.updateKeyFrameDescInfos(model.keyFrameDescInfos);
                    if (model.videoInfo != null && !TextUtils.isEmpty(model.videoInfo.coverUrl)) {
                    }

                }

                public void onFail(int errCode, String message) {
                    Log.i("SuperPlayerView", "onFail: errorCode = " + errCode + " message = " + message);
                    Toast.makeText(MySuperVideoView.this.getContext(), "播放视频文件失败 code = " + errCode + " msg = " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            this.stopPlay();
            this.initLivePlayer(this.getContext());
            this.initVodPlayer(this.getContext());
            ArrayList<TCVideoQulity> videoQualities = new ArrayList();
            String videoURL = null;
            if (modelWrapper.requestModel.multiURLs != null && !modelWrapper.requestModel.multiURLs.isEmpty()) {
                int i = 0;

                SuperPlayerModel.SuperPlayerURL superPlayerURL;
                for(Iterator var6 = modelWrapper.requestModel.multiURLs.iterator(); var6.hasNext(); videoQualities.add(new TCVideoQulity(i++, superPlayerURL.qualityName, superPlayerURL.url))) {
                    superPlayerURL = (SuperPlayerModel.SuperPlayerURL)var6.next();
                    if (i == modelWrapper.requestModel.playDefaultIndex) {
                        videoURL = superPlayerURL.url;
                    }
                }

                this.mVodControllerLarge.setVideoQualityList(videoQualities);
                this.mVodControllerLarge.updateVideoQulity((TCVideoQulity)videoQualities.get(modelWrapper.requestModel.playDefaultIndex));
            } else if (!TextUtils.isEmpty(modelWrapper.requestModel.url)) {
                videoQualities.add(new TCVideoQulity(0, modelWrapper.requestModel.qualityName, modelWrapper.requestModel.url));
                videoURL = modelWrapper.requestModel.url;
            }

            if (TextUtils.isEmpty(videoURL)) {
                Toast.makeText(this.getContext(), "播放视频失败，播放连接为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (this.isRTMPPlay(videoURL)) {
                this.mReportLiveStartTime = System.currentTimeMillis();
                this.mLivePlayer.setPlayerView(this.mTXCloudVideoView);
                this.playLiveURL(videoURL, 0);
            } else if (this.isFLVPlay(videoURL)) {
                this.mReportLiveStartTime = System.currentTimeMillis();
                this.mLivePlayer.setPlayerView(this.mTXCloudVideoView);
                this.playTimeShiftLiveURL(modelWrapper);
                if (modelWrapper.requestModel.multiURLs != null && !modelWrapper.requestModel.multiURLs.isEmpty()) {
                    this.startMultiStreamLiveURL(videoURL);
                }
            } else {
                this.mReportVodStartTime = System.currentTimeMillis();
                this.mVodPlayer.setPlayerView(this.mTXCloudVideoView);
                this.playVodURL(videoURL);
            }

            boolean isLivePlay = this.isRTMPPlay(videoURL) || this.isFLVPlay(videoURL);
            this.mCurrentPlayType = isLivePlay ? 2 : 1;
            this.mVodControllerSmall.updatePlayType(isLivePlay ? 2 : 1);
            this.mVodControllerLarge.updatePlayType(isLivePlay ? 2 : 1);
            this.mVodControllerSmall.updateTitle(modelWrapper.requestModel.title);
            this.mVodControllerLarge.updateTitle(modelWrapper.requestModel.title);
            this.mVodControllerSmall.updatePlayState(true);
            this.mVodControllerLarge.updatePlayState(true);
            this.mVodControllerSmall.updateVideoProgress(0L, 0L);
            this.mVodControllerLarge.updateVideoProgress(0L, 0L);
        }

    }



    private int calculateInSampleSize(int inWidth, int inHeight, int outWidth, int outHeight) {
        int inSampleSize = 1;
        if (inHeight > outHeight || inWidth > outWidth) {
            int heightRatio = Math.round((float)inHeight / (float)outHeight);
            int widthRatio = Math.round((float)inWidth / (float)outWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private void playV3ModelVideo(SuperPlayerModelWrapper model) {
    }

    private void playLiveURL(String url, int playType) {
        this.mCurrentPlayVideoURL = url;
        if (this.mLivePlayer != null) {
            this.mLivePlayer.setPlayListener(this);
            int result = this.mLivePlayer.startPlay(url, playType);
            if (result != 0) {
                TXCLog.e("SuperPlayerView", "playLiveURL videoURL:" + url + ",result:" + result);
            } else {
                this.mCurrentPlayState = 1;
                TXCLog.e("SuperPlayerView", "playLiveURL mCurrentPlayState:" + this.mCurrentPlayState);
            }
        }

    }

    private void playVodURL(String url) {
        this.mCurrentPlayVideoURL = url;
        TXCLog.i("SuperPlayerView", "playVodURL videoURL:" + url);
        if (url.contains(".m3u8")) {
            this.mIsMultiBitrateStream = true;
        }

        if (this.mVodPlayer != null) {
            this.mDefaultSet = false;
//            this.mVodPlayer.setAutoPlay(true);
            this.mVodPlayer.setVodListener(this);
            int ret = this.mVodPlayer.startPlay(url);
            if (ret == 0) {
                this.mCurrentPlayState = 1;
                TXCLog.e("SuperPlayerView", "playVodURL mCurrentPlayState:" + this.mCurrentPlayState);
            }
        }

        this.mIsPlayWithFileid = false;
    }

    private boolean isRTMPPlay(String videoURL) {
        return !TextUtils.isEmpty(videoURL) && videoURL.startsWith("rtmp");
    }

    private boolean isFLVPlay(String videoURL) {
        return (!TextUtils.isEmpty(videoURL) && videoURL.startsWith("http://") || videoURL.startsWith("https://")) && videoURL.contains(".flv");
    }

    private void playTimeShiftLiveURL(SuperPlayerModelWrapper model) {
        String liveURL = model.requestModel.url;
        String bizid = liveURL.substring(liveURL.indexOf("//") + 2, liveURL.indexOf("."));
        String domian = SuperPlayerGlobalConfig.getInstance().playShiftDomain;
        String streamid = liveURL.substring(liveURL.lastIndexOf("/") + 1, liveURL.lastIndexOf("."));
        int appid = model.requestModel.appId;
        TXCLog.i("SuperPlayerView", "bizid:" + bizid + ",streamid:" + streamid + ",appid:" + appid);
        this.playLiveURL(liveURL, 1);

        try {
            int bizidNum = Integer.valueOf(bizid);
            this.mLivePlayer.prepareLiveSeek(domian, bizidNum);
        } catch (NumberFormatException var8) {
            var8.printStackTrace();
            Log.e("SuperPlayerView", "playTimeShiftLiveURL: bizidNum 错误 = %s " + bizid);
        }

    }

    private void startMultiStreamLiveURL(String url) {
        this.mLivePlayConfig.setAutoAdjustCacheTime(false);
        this.mLivePlayConfig.setMaxAutoAdjustCacheTime(5.0F);
        this.mLivePlayConfig.setMinAutoAdjustCacheTime(5.0F);
        this.mLivePlayer.setConfig(this.mLivePlayConfig);
        if (this.mWatcher == null) {
            this.mWatcher = new NetWatcher(this.mContext);
        }

        this.mWatcher.start(url, this.mLivePlayer);
    }

    private void playV2ModelVideo(SuperPlayerModelWrapper modelV3) {
        if (modelV3.playInfoResponseParser != null) {
            TCPlayInfoStream masterPlayList = modelV3.playInfoResponseParser.getMasterPlayList();
            modelV3.imageInfo = modelV3.playInfoResponseParser.getImageSpriteInfo();
            modelV3.keyFrameDescInfos = modelV3.playInfoResponseParser.getKeyFrameDescInfos();
            if (masterPlayList != null) {
                String videoURL = masterPlayList.getUrl();
                this.playVodURL(videoURL);
                this.mIsMultiBitrateStream = true;
                this.mIsPlayWithFileid = true;
            } else {
                LinkedHashMap<String, TCPlayInfoStream> transcodeList = modelV3.playInfoResponseParser.getTranscodePlayList();
                String videoURL;
                if (transcodeList != null && transcodeList.size() != 0) {
                    String defaultClassification = modelV3.playInfoResponseParser.getDefaultVideoClassification();
                    TCPlayInfoStream stream = (TCPlayInfoStream)transcodeList.get(defaultClassification);
                    videoURL = null;
                    if (stream != null) {
                        videoURL = stream.getUrl();
                    } else {
                        Iterator var7 = transcodeList.values().iterator();

                        while(var7.hasNext()) {
                            TCPlayInfoStream stream1 = (TCPlayInfoStream)var7.next();
                            if (stream1 != null && stream1.getUrl() != null) {
                                stream = stream1;
                                videoURL = stream1.getUrl();
                                break;
                            }
                        }
                    }

                    if (videoURL != null) {
                        this.playVodURL(videoURL);
                        ArrayList<TCVideoQulity> videoQulities = SuperPlayerUtil.convertToVideoQualityList(transcodeList);
                        this.mVodControllerLarge.setVideoQualityList(videoQulities);
                        TCVideoQulity defaultVideoQulity = SuperPlayerUtil.convertToVideoQuality(stream);
                        this.mVodControllerLarge.updateVideoQulity(defaultVideoQulity);
                        this.mIsMultiBitrateStream = false;
                        this.mIsPlayWithFileid = true;
                        return;
                    }
                }

                TCPlayInfoStream sourceStream = modelV3.playInfoResponseParser.getSource();
                if (sourceStream != null) {
                    String url = sourceStream.getUrl();
                    this.playVodURL(url);
                    videoURL = modelV3.playInfoResponseParser.getDefaultVideoClassification();
                    if (videoURL != null) {
                        TCVideoQulity defaultVideoQulity = SuperPlayerUtil.convertToVideoQuality(sourceStream, videoURL);
                        this.mVodControllerLarge.updateVideoQulity(defaultVideoQulity);
                        ArrayList<TCVideoQulity> videoQulities = new ArrayList();
                        videoQulities.add(defaultVideoQulity);
                        this.mVodControllerLarge.setVideoQualityList(videoQulities);
                        this.mIsMultiBitrateStream = false;
                    }
                }

            }
        }
    }




    private void resume() {
        if (this.mCurrentPlayType == 1 && this.mVodPlayer != null) {
            this.mVodPlayer.resume();
            this.mVodControllerSmall.updatePlayState(true);
            this.mVodControllerLarge.updatePlayState(true);
            if (this.mCurrentModelWrapper != null && this.mCurrentModelWrapper.currentPlayingType == 0 && this.mCurrentTimeWhenPause != 0.0F) {
                this.mVodPlayer.seek(this.mCurrentTimeWhenPause);
                this.mCurrentTimeWhenPause = 0.0F;
            }
        }

    }




    private void pause() {
        if (this.mCurrentPlayType == 1 && this.mVodPlayer != null) {
            if (this.mCurrentModelWrapper != null && this.mCurrentModelWrapper.currentPlayingType == 0) {
                this.mCurrentTimeWhenPause = this.mVodPlayer.getCurrentPlaybackTime();
            }
            this.mVodControllerSmall.updatePlayState(false);
            this.mVodControllerLarge.updatePlayState(false);
            this.mVodPlayer.pause();
        }

    }



    private void stopPlay() {
        if (this.mVodPlayer != null) {
            this.mVodPlayer.setVodListener((ITXVodPlayListener)null);
            this.mVodPlayer.stopPlay(false);
        }

        if (this.mLivePlayer != null) {
            this.mLivePlayer.setPlayListener((ITXLivePlayListener)null);
            this.mLivePlayer.stopPlay(false);
            this.mTXCloudVideoView.removeVideoView();
        }

        if (this.mWatcher != null) {
            this.mWatcher.stop();
        }

        this.mCurrentPlayState = 2;
        TXCLog.e("SuperPlayerView", "stopPlay mCurrentPlayState:" + this.mCurrentPlayState);
        this.reportPlayTime();
    }

    private void reportPlayTime() {
        long reportEndTime;
        long diff;
        if (this.mReportLiveStartTime != -1L) {
            reportEndTime = System.currentTimeMillis();
            diff = (reportEndTime - this.mReportLiveStartTime) / 1000L;
            LogReport.getInstance().uploadLogs("superlive", diff, 0);
            this.mReportLiveStartTime = -1L;
        }

        if (this.mReportVodStartTime != -1L) {
            reportEndTime = System.currentTimeMillis();
            diff = (reportEndTime - this.mReportVodStartTime) / 1000L;
            LogReport.getInstance().uploadLogs("supervod", diff, this.mIsPlayWithFileid ? 1 : 0);
            this.mReportVodStartTime = -1L;
        }

    }



    private void fullScreen(boolean isFull) {
        if (this.getContext() instanceof Activity) {
            Activity activity = (Activity)this.getContext();
            View decorView;
            if (isFull) {
                decorView = activity.getWindow().getDecorView();
                if (decorView == null) {
                    return;
                }
                WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
//                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
//                    decorView.setSystemUiVisibility(8);
//                } else if (Build.VERSION.SDK_INT >= 19) {
//                    int uiOptions = 4102;
//                    decorView.setSystemUiVisibility(uiOptions);
//                }
            } else {
                decorView = activity.getWindow().getDecorView();
                if (decorView == null) {
                    return;
                }
                WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
//                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
//                    decorView.setSystemUiVisibility(0);
//                } else if (Build.VERSION.SDK_INT >= 19) {
//                    decorView.setSystemUiVisibility(0);
//                }
                setStatusStyle();
            }
        }

    }

    public void setStatusStyle() {
        int statusColorRes = getResources().getColor(R.color.black);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 设置状态栏底色白色
            Activity activity = (Activity)this.getContext();
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(statusColorRes);
            // 设置状态栏字体黑色
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void showSnapshotWindow(final Bitmap bmp) {
        final PopupWindow popupWindow = new PopupWindow(this.mContext);
        popupWindow.setWidth(-2);
        popupWindow.setHeight(-2);
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.layout_new_vod_snap, (ViewGroup)null);
        ImageView imageView = (ImageView)view.findViewById(R.id.iv_snap);
        imageView.setImageBitmap(bmp);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(this.mRootView, 48, 1800, 300);
        AsyncTask.execute(new Runnable() {
            public void run() {
                MySuperVideoView.this.save2MediaStore(bmp);
            }
        });
        this.postDelayed(new Runnable() {
            public void run() {
                popupWindow.dismiss();
            }
        }, 3000L);
    }

    @SuppressLint({"WrongThread"})
    private void save2MediaStore(Bitmap image) {
        try {
            File appDir = new File(Environment.getExternalStorageDirectory(), "superplayer");
            if (!appDir.exists()) {
                appDir.mkdir();
            }

            long dateSeconds = System.currentTimeMillis() / 1000L;
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            String filePath = file.getAbsolutePath();
            ContentValues values = new ContentValues();
            ContentResolver resolver = this.mContext.getContentResolver();
            values.put("_data", filePath);
            values.put("title", fileName);
            values.put("_display_name", fileName);
            values.put("date_added", dateSeconds);
            values.put("date_modified", dateSeconds);
            values.put("mime_type", "image/png");
            values.put("width", image.getWidth());
            values.put("height", image.getHeight());
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream out = resolver.openOutputStream(uri);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            values.clear();
            values.put("_size", (new File(filePath)).length());
            resolver.update(uri, values, (String)null, (String[])null);
        } catch (Exception var12) {
        }

    }

    private void rotateScreenOrientation(int orientation) {
        switch(orientation) {
            case 1:
                int angle = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                if(angle ==3){
                    ((Activity)this.mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }else{
                    ((Activity)this.mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case 2:
                ((Activity)this.mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != 2005) {
            String playEventLog = "TXVodPlayer onPlayEvent event: " + event + ", " + param.getString("EVT_MSG");
            TXCLog.d("SuperPlayerView", playEventLog);
        }
        if(event==2007){
            long progress = (long) (mVodPlayer.getCurrentPlaybackTime()*1000);
            Log.i("DYLOG", "当前进度："+progress);
            if(progress>notVipCanLookTimes){
                //超过1分钟，回调接口
                if(onCanLookTimeOverListener!=null){
                    onCanLookTimeOverListener.onSeekBar();
                }
            }
        }
        if (event == 2013) {
            this.mVodControllerSmall.showLoadingView(false);
            this.mVodControllerLarge.showLoadingView(false);
            this.mVodControllerSmall.dismissBackground();
            this.mVodControllerSmall.updateLiveLoadingState(false);
            this.mVodControllerLarge.updateLiveLoadingState(false);
            this.mVodControllerSmall.updatePlayState(true);
            this.mVodControllerLarge.updatePlayState(true);
            this.mVodControllerSmall.updateReplay(false);
            this.mVodControllerLarge.updateReplay(false);
            if (this.mIsMultiBitrateStream) {
                ArrayList<TXBitrateItem> bitrateItems = this.mVodPlayer.getSupportedBitrates();
                if (bitrateItems == null || bitrateItems.size() == 0) {
                    return;
                }

                Collections.sort(bitrateItems);
                ArrayList<TCVideoQulity> videoQulities = new ArrayList();
                int size = bitrateItems.size();

                TXBitrateItem bitrateItem;
                TCVideoQulity defaultVideoQuality;
                for(int i = 0; i < size; ++i) {
                    bitrateItem = (TXBitrateItem)bitrateItems.get(i);
                    defaultVideoQuality = SuperPlayerUtil.convertToVideoQuality(bitrateItem, i);
                    videoQulities.add(defaultVideoQuality);
                }

                if (!this.mDefaultSet) {
                    TXBitrateItem defaultitem = (TXBitrateItem)bitrateItems.get(bitrateItems.size() - 1);
                    this.mVodPlayer.setBitrateIndex(defaultitem.index);
                    bitrateItem = (TXBitrateItem)bitrateItems.get(bitrateItems.size() - 1);
                    defaultVideoQuality = SuperPlayerUtil.convertToVideoQuality(bitrateItem, bitrateItems.size() - 1);
                    this.mVodControllerLarge.updateVideoQulity(defaultVideoQuality);
                    this.mDefaultSet = true;
                }

                this.mVodControllerLarge.setVideoQualityList(videoQulities);
            }
        }else if(event==2103){
            this.mVodControllerSmall.showLoadingView(true);
            this.mVodControllerLarge.showLoadingView(true);
        } else if (event == 2003) {
            if (this.mChangeHWAcceleration) {
                TXCLog.i("SuperPlayerView", "seek pos:" + this.mSeekPos);
                this.mVodController.seekTo(this.mSeekPos);
                this.mChangeHWAcceleration = false;
            }
        }else if(event==2007){
            this.mVodControllerSmall.showLoadingView(true);
            this.mVodControllerLarge.showLoadingView(true);
        }else if(event==2014){
            this.mVodControllerSmall.showLoadingView(false);
            this.mVodControllerLarge.showLoadingView(false);
        } else if (event == 2006) {//视频播放结束
            this.mCurrentPlayState = 2;
            this.mVodControllerSmall.updatePlayState(false);
            this.mVodControllerLarge.updatePlayState(false);
            this.mVodControllerSmall.updateReplay(true);
            this.mVodControllerLarge.updateReplay(true);
        } else if (event == 2005) {
            int progress = param.getInt("EVT_PLAY_PROGRESS_MS");
            int duration = param.getInt("EVT_PLAY_DURATION_MS");
            this.mVodControllerSmall.updateVideoProgress((long)(progress / 1000), (long)(duration / 1000));
            this.mVodControllerLarge.updateVideoProgress((long)(progress / 1000), (long)(duration / 1000));
            if(progress>notVipCanLookTimes){
                //超过1分钟，回调接口
                if(onCanLookTimeOverListener!=null){
                    onCanLookTimeOverListener.onTimeOver();
                }
            }
        } else if (event == -2305 || event == -2301) {//网络断连,且经多次重连亦不能恢复,更多重试请自行重启播放
            this.mVodPlayer.stopPlay(true);
            this.mVodControllerSmall.updatePlayState(false);
            this.mVodControllerLarge.updatePlayState(false);
            boolean isV3Protocol = this.mCurrentModelWrapper != null && this.mCurrentModelWrapper.isV3Protocol();
            if (isV3Protocol) {
                Log.i("SuperPlayerView", "onPlayEvent: play type = " + this.mCurrentModelWrapper.currentPlayingType + " video fail.");
                Pair<Integer, String> pair = this.mCurrentModelWrapper.getNextURL(this.mCurrentModelWrapper.currentPlayingType);
                if (pair != null) {
                    this.mCurrentModelWrapper.currentPlayingType = (Integer)pair.first;
                    String url = (String)pair.second;
                    if ((Integer)pair.first != 0 && (Integer)pair.first != 1) {
                        this.mVodPlayer.setToken((String)null);
                    }

                    Log.i("SuperPlayerView", "onPlayEvent: try play type = " + this.mCurrentModelWrapper.currentPlayingType + " video = " + url);
                    if (url != null) {
                        this.playVodURL(url);
                    } else {
                        Log.e("发生错误：", param.getString("EVT_MSG"));
                        if(onErrPlayListener!=null){
                            onErrPlayListener.onErroPlayed(param.getString("EVT_MSG"));
                        }
                    }
                } else {
                    Log.e("发生错误：", param.getString("EVT_MSG"));
                    if(onErrPlayListener!=null){
                        onErrPlayListener.onErroPlayed(param.getString("EVT_MSG"));
                    }
                }
            } else {
                Log.e("发生错误：", param.getString("EVT_MSG"));
                if(onErrPlayListener!=null){
                    onErrPlayListener.onErroPlayed(param.getString("EVT_MSG"));
                }
            }
        }

        if (event < 0 && event != -2305 && event != -2301) {
            this.mVodPlayer.stopPlay(true);
            this.mVodControllerSmall.updatePlayState(false);
            this.mVodControllerLarge.updatePlayState(false);
            Log.e("发生错误：", param.getString("EVT_MSG"));
            if(onErrPlayListener!=null){
                onErrPlayListener.onErroPlayed(param.getString("EVT_MSG"));
            }
        }

    }


    OnErrPlayListener onErrPlayListener;

    public void setOnErrPlayListener(OnErrPlayListener onErrPlayListener) {
        this.onErrPlayListener = onErrPlayListener;
    }

    @Override
    public void onItemChecked(float speed, TextView textView) {
        if(mVodPlayer!=null){
            mVodPlayer.setRate(speed);
            if(speed==1.0f){
                mVodControllerSmall.mTvSpeed.setText("倍速");
                mVodControllerLarge.mTvSpeed.setText("倍速");
            }else if(speed==0.75f){
                mVodControllerSmall.mTvSpeed.setText("0.75X");
                mVodControllerLarge.mTvSpeed.setText("0.75X");
            }else if(speed==1.5f){
                mVodControllerSmall.mTvSpeed.setText("1.5X");
                mVodControllerLarge.mTvSpeed.setText("1.5X");
            }else if(speed==2.0f){
                mVodControllerSmall.mTvSpeed.setText("2.0X");
                mVodControllerLarge.mTvSpeed.setText("2.0X");
            }
        }
    }

    public interface OnErrPlayListener{
        void onErroPlayed(String reason);
    }

    public void onNetStatus(TXVodPlayer player, Bundle status) {
    }

    public OnCanLookTimeOverListener onCanLookTimeOverListener;

    public void setOnCanLookTimeOverListener(OnCanLookTimeOverListener onCanLookTimeOverListener) {
        this.onCanLookTimeOverListener = onCanLookTimeOverListener;
    }

    public interface OnCanLookTimeOverListener{
        void onTimeOver();
        void onSeekBar();
    }

    public void onPlayEvent(int event, Bundle param) {
        if (event != 2005) {
            String playEventLog = "TXLivePlayer onPlayEvent event: " + event + ", " + param.getString("EVT_MSG");
            TXCLog.d("SuperPlayerView", playEventLog);
        }
        if(event==2007){
            long progress = (long) (mVodPlayer.getCurrentPlaybackTime()*1000);
            Log.i("DYLOG", "当前进度："+progress);
            if(progress>notVipCanLookTimes){
                //超过1分钟，回调接口
                if(onCanLookTimeOverListener!=null){
                    onCanLookTimeOverListener.onSeekBar();
                }
            }
        }
        if (event == 2013) {
            this.mVodControllerSmall.showLoadingView(false);
            this.mVodControllerLarge.showLoadingView(false);
            this.mVodControllerSmall.updateLiveLoadingState(false);
            this.mVodControllerLarge.updateLiveLoadingState(false);
            this.mVodControllerSmall.updatePlayState(true);
            this.mVodControllerLarge.updatePlayState(true);
            this.mVodControllerSmall.updateReplay(false);
            this.mVodControllerLarge.updateReplay(false);
         }else if(event==2103){
            this.mVodControllerSmall.showLoadingView(true);
            this.mVodControllerLarge.showLoadingView(true);
        }else if(event==2014){
            this.mVodControllerSmall.showLoadingView(false);
            this.mVodControllerLarge.showLoadingView(false);
        } else if (event == 2004) {
            this.mVodControllerSmall.showLoadingView(false);
            this.mVodControllerLarge.showLoadingView(false);
            this.mVodControllerSmall.updateLiveLoadingState(false);
            this.mVodControllerLarge.updateLiveLoadingState(false);
            this.mVodControllerSmall.updatePlayState(true);
            this.mVodControllerLarge.updatePlayState(true);
            this.mVodControllerSmall.updateReplay(false);
            this.mVodControllerLarge.updateReplay(false);
            if (this.mWatcher != null) {
                this.mWatcher.exitLoading();
            }
        } else if (event != -2301 && event != 2006) {
            if (event == 2007) {
                this.mVodControllerSmall.showLoadingView(true);
                this.mVodControllerLarge.showLoadingView(true);
                this.mVodControllerSmall.updateLiveLoadingState(true);
                this.mVodControllerLarge.updateLiveLoadingState(true);
                if (this.mWatcher != null) {
                    this.mWatcher.enterLoading();
                }
            }else if(event==2017){
                this.mVodControllerSmall.showLoadingView(false);
                this.mVodControllerLarge.showLoadingView(false);
            } else if (event != 2003 && event != 2009) {
                if (event == 2011) {
                    return;
                }

                if (event == 2015) {
                    Toast.makeText(this.mContext, "清晰度切换成功", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (event == -2307) {
                    Toast.makeText(this.mContext, "清晰度切换失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (event == 2005) {
                    int progress = param.getInt("EVT_PLAY_PROGRESS_MS");
                    if ((long)progress > this.mMaxLiveProgressTime) {
                        this.mMaxLiveProgressTime = (long)progress;
                    }
                    if(progress>notVipCanLookTimes){
                        //超过1分钟，回调接口
                        if(onCanLookTimeOverListener!=null){
                            onCanLookTimeOverListener.onTimeOver();
                        }
                    }
                    this.mVodControllerSmall.updateVideoProgress((long)(progress / 1000), this.mMaxLiveProgressTime / 1000L);
                    this.mVodControllerLarge.updateVideoProgress((long)(progress / 1000), this.mMaxLiveProgressTime / 1000L);
                }
            }
        } else if (this.mCurrentPlayType == 3) {
            this.mVodController.resumeLive();
            Toast.makeText(this.mContext, "时移失败,返回直播", Toast.LENGTH_SHORT).show();
            this.mVodControllerSmall.updateReplay(false);
            this.mVodControllerLarge.updateReplay(false);
            this.mVodControllerSmall.updateLiveLoadingState(false);
            this.mVodControllerLarge.updateLiveLoadingState(false);
        } else {
            this.stopPlay();
            this.mVodControllerSmall.updatePlayState(false);
            this.mVodControllerLarge.updatePlayState(false);
            this.mVodControllerSmall.updateReplay(true);
            this.mVodControllerLarge.updateReplay(true);
            if (event == -2301) {
                Toast.makeText(this.mContext, "网络不给力,点击重试", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("发生错误：", param.getString("EVT_MSG"));
                if(onErrPlayListener!=null){
                    onErrPlayListener.onErroPlayed(param.getString("EVT_MSG"));
                }
            }
        }

    }

    public void onNetStatus(Bundle status) {
    }

    public void requestPlayMode(int playMode) {
        if (playMode == 1) {
            if (this.mVodController != null) {
                this.mVodController.onRequestPlayMode(1);
            }
        } else if (playMode == 3) {
            if (this.mPlayerViewCallback != null) {
                this.mPlayerViewCallback.onStartFloatWindowPlay();
            }

            if (this.mVodController != null) {
                this.mVodController.onRequestPlayMode(3);
            }
        }

    }

    private boolean checkOp(Context context, int op) {
        if (Build.VERSION.SDK_INT >= 19) {
            AppOpsManager manager = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);

            try {
                Method method = AppOpsManager.class.getDeclaredMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                return 0 == (Integer)method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception var5) {
                Log.e("SuperPlayerView", Log.getStackTraceString(var5));
            }
        }

        return true;
    }



    public void release() {
        if (this.mVodControllerSmall != null) {
            this.mVodControllerSmall.release();
        }

        if (this.mVodControllerLarge != null) {
            this.mVodControllerLarge.release();
        }

        if (this.mVodControllerFloat != null) {
            this.mVodControllerFloat.release();
        }

    }

    protected void finalize() throws Throwable {
        super.finalize();

        try {
            this.release();
        } catch (Exception var2) {
        } catch (Error var3) {
        }

    }

    public interface OnSuperPlayerViewCallback {
        void onStartFullScreenPlay();

        void onStopFullScreenPlay();

        void onClickFloatCloseBtn();

        void onClickSmallReturnBtn();

        void onStartFloatWindowPlay();


        void onFullBackButtonClick();
    }


   public class NamelessClass_1 implements TCVodControllerBase.VodController {
        NamelessClass_1() {
        }

        public void onRequestPlayMode(int requestPlayMode) {
            if (MySuperVideoView.this.mPlayMode != requestPlayMode) {
                if (!MySuperVideoView.this.mLockScreen) {
                    if (requestPlayMode == 2) {
                        MySuperVideoView.this.fullScreen(true);
                    } else {
                        MySuperVideoView.this.fullScreen(false);
                    }

                    MySuperVideoView.this.mVodControllerFloat.hide();
                    MySuperVideoView.this.mVodControllerSmall.hide();
                    MySuperVideoView.this.mVodControllerLarge.hide();
                    if (requestPlayMode == 2) {
                        TXCLog.i("SuperPlayerView", "requestPlayMode FullScreen");
                        if (MySuperVideoView.this.mLayoutParamFullScreenMode == null) {
                            return;
                        }

                        MySuperVideoView.this.removeView(MySuperVideoView.this.mVodControllerSmall);
                        MySuperVideoView.this.addView(MySuperVideoView.this.mVodControllerLarge, MySuperVideoView.this.mVodControllerLargeParams);
                        MySuperVideoView.this.setLayoutParams(MySuperVideoView.this.mLayoutParamFullScreenMode);
                        MySuperVideoView.this.mVodControllerLarge.show();
                        MySuperVideoView.this.mVodControllerLarge.refreshVoiceStatus();
                        MySuperVideoView.this.rotateScreenOrientation(1);
                        if (MySuperVideoView.this.mPlayerViewCallback != null) {
                            MySuperVideoView.this.mPlayerViewCallback.onStartFullScreenPlay();
                        }
                        MySuperVideoView.this.mVodControllerLarge.showAnimation();
                    } else {
                        Intent intent;
                        if (requestPlayMode == 1) {
                            TXCLog.i("SuperPlayerView", "requestPlayMode Window");
                            if (MySuperVideoView.this.mPlayMode == 3) {
                                try {
                                    Context viewContext = MySuperVideoView.this.getContext();
                                    intent = null;
                                    if (!(viewContext instanceof Activity)) {
                                        Toast.makeText(viewContext, "悬浮播放失败", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    intent = new Intent(MySuperVideoView.this.getContext(), viewContext.getClass());
                                    MySuperVideoView.this.mContext.startActivity(intent);
                                    float currentTimex = MySuperVideoView.this.mVodPlayer.getCurrentPlaybackTime();
                                    this.pause();
                                    if (MySuperVideoView.this.mLayoutParamWindowMode == null) {
                                        return;
                                    }

                                    MySuperVideoView.this.mWindowManager.removeView(MySuperVideoView.this.mVodControllerFloat);
                                    if (MySuperVideoView.this.mCurrentPlayType == 1) {
                                        MySuperVideoView.this.mVodPlayer.setPlayerView(MySuperVideoView.this.mTXCloudVideoView);
                                    } else {
                                        MySuperVideoView.this.mLivePlayer.setPlayerView(MySuperVideoView.this.mTXCloudVideoView);
                                    }

                                    this.resume();
                                    if (MySuperVideoView.this.mCurrentModelWrapper != null && MySuperVideoView.this.mCurrentModelWrapper.currentPlayingType == 0) {
                                        MySuperVideoView.this.mVodPlayer.seek(currentTimex);
                                    }
                                } catch (Exception var7) {
                                    var7.printStackTrace();
                                }
                            } else if (MySuperVideoView.this.mPlayMode == 2) {
                                if (MySuperVideoView.this.mLayoutParamWindowMode == null) {
                                    return;
                                }

                                MySuperVideoView.this.removeView(MySuperVideoView.this.mVodControllerLarge);
                                MySuperVideoView.this.addView(MySuperVideoView.this.mVodControllerSmall, MySuperVideoView.this.mVodControllerSmallParams);
                                MySuperVideoView.this.setLayoutParams(MySuperVideoView.this.mLayoutParamWindowMode);
                                MySuperVideoView.this.rotateScreenOrientation(2);
                                if (MySuperVideoView.this.mPlayerViewCallback != null) {
                                    MySuperVideoView.this.mPlayerViewCallback.onStopFullScreenPlay();
                                }
                                MySuperVideoView.this.mVodControllerSmall.refreshVoiceStatus();
                                MySuperVideoView.this.mVodControllerSmall.showAnimation();
                            }
                        } else if (requestPlayMode == 3) {
                            TXCLog.i("SuperPlayerView", "requestPlayMode Float :" + Build.MANUFACTURER);
                            SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
                            if (!prefs.enableFloatWindow) {
                                return;
                            }

                            if (Build.VERSION.SDK_INT >= 23) {
                                if (!Settings.canDrawOverlays(MySuperVideoView.this.mContext)) {
                                    intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
                                    intent.setData(Uri.parse("package:" + MySuperVideoView.this.mContext.getPackageName()));
                                    MySuperVideoView.this.mContext.startActivity(intent);
                                    return;
                                }
                            } else if (!MySuperVideoView.this.checkOp(MySuperVideoView.this.mContext, 24)) {
                                Toast.makeText(MySuperVideoView.this.mContext, "进入设置页面失败,请手动开启悬浮窗权限", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            float currentTime = MySuperVideoView.this.mVodPlayer.getCurrentPlaybackTime();
                            this.pause();
                            MySuperVideoView.this.mWindowManager = (WindowManager)mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                            MySuperVideoView.this.mWindowParams = new WindowManager.LayoutParams();
                            if (Build.VERSION.SDK_INT >= 26) {
                                MySuperVideoView.this.mWindowParams.type = 2038;
                            } else {
                                MySuperVideoView.this.mWindowParams.type = 2002;
                            }

                            MySuperVideoView.this.mWindowParams.flags = 40;
                            MySuperVideoView.this.mWindowParams.format = -3;
                            MySuperVideoView.this.mWindowParams.gravity = 51;
                            SuperPlayerGlobalConfig.TXRect rect = prefs.floatViewRect;
                            MySuperVideoView.this.mWindowParams.x = rect.x;
                            MySuperVideoView.this.mWindowParams.y = rect.y;
                            MySuperVideoView.this.mWindowParams.width = rect.width;
                            MySuperVideoView.this.mWindowParams.height = rect.height;

                            try {
                                MySuperVideoView.this.mWindowManager.addView(MySuperVideoView.this.mVodControllerFloat, MySuperVideoView.this.mWindowParams);
                            } catch (Exception var6) {
                                Toast.makeText(MySuperVideoView.this.getContext(), "悬浮播放失败", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            TXCloudVideoView videoView = MySuperVideoView.this.mVodControllerFloat.getFloatVideoView();
                            if (videoView != null) {
                                if (MySuperVideoView.this.mCurrentPlayType == 1) {
                                    MySuperVideoView.this.mVodPlayer.setPlayerView(videoView);
                                } else {
                                    MySuperVideoView.this.mLivePlayer.setPlayerView(videoView);
                                }

                                this.resume();
                                if (MySuperVideoView.this.mCurrentModelWrapper != null && MySuperVideoView.this.mCurrentModelWrapper.currentPlayingType == 0) {
                                    MySuperVideoView.this.mVodPlayer.seek(currentTime);
                                }
                            }

                            LogReport.getInstance().uploadLogs("floatmode", 0L, 0);
                        }
                    }

                    MySuperVideoView.this.mPlayMode = requestPlayMode;
                }
            }
        }

        public void onBackPress(int playMode) {
            if (playMode == LARGE_CONTROLLER_MODE) {
                this.onRequestPlayMode(SMALL_CONTROLLER_MODE);
                if (MySuperVideoView.this.mPlayerViewCallback != null) {
                    MySuperVideoView.this.mPlayerViewCallback.onFullBackButtonClick();
                }
            } else if (playMode == SMALL_CONTROLLER_MODE) {
                if (MySuperVideoView.this.mPlayerViewCallback != null) {
                    MySuperVideoView.this.mPlayerViewCallback.onClickSmallReturnBtn();
                }
            } else if (playMode == FLOAT_CONTROLLER_MODE) {
                MySuperVideoView.this.mWindowManager.removeView(MySuperVideoView.this.mVodControllerFloat);
                if (MySuperVideoView.this.mPlayerViewCallback != null) {
                    MySuperVideoView.this.mPlayerViewCallback.onClickFloatCloseBtn();
                }
                this.onRequestPlayMode(SMALL_CONTROLLER_MODE);
            }

        }

        public void resume() {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                if (MySuperVideoView.this.mVodPlayer != null) {
                    MySuperVideoView.this.mVodPlayer.resume();
                    if (MySuperVideoView.this.mCurrentModelWrapper != null && MySuperVideoView.this.mCurrentModelWrapper.currentPlayingType == 0 && MySuperVideoView.this.mCurrentTimeWhenPause != 0.0F) {
                        MySuperVideoView.this.mVodPlayer.seek(MySuperVideoView.this.mCurrentTimeWhenPause);
                        MySuperVideoView.this.mCurrentTimeWhenPause = 0.0F;
                    }
                }
            } else if (MySuperVideoView.this.mLivePlayer != null) {
                MySuperVideoView.this.mLivePlayer.resume();
            }

            MySuperVideoView.this.mCurrentPlayState = 1;
            MySuperVideoView.this.mVodControllerSmall.updatePlayState(true);
            MySuperVideoView.this.mVodControllerLarge.updatePlayState(true);
            MySuperVideoView.this.mVodControllerLarge.updateReplay(false);
            MySuperVideoView.this.mVodControllerSmall.updateReplay(false);
        }

        public void pause() {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                if (MySuperVideoView.this.mVodPlayer != null) {
                    MySuperVideoView.this.mVodPlayer.pause();
                }
            } else {
                if (MySuperVideoView.this.mLivePlayer != null) {
                    MySuperVideoView.this.mLivePlayer.pause();
                }

                if (MySuperVideoView.this.mWatcher != null) {
                    MySuperVideoView.this.mWatcher.stop();
                }
            }

            MySuperVideoView.this.mCurrentPlayState = 2;
            TXCLog.e("lyj", "pause mCurrentPlayState:" + MySuperVideoView.this.mCurrentPlayState);
            MySuperVideoView.this.mVodControllerSmall.updatePlayState(false);
            MySuperVideoView.this.mVodControllerLarge.updatePlayState(false);
        }

        public float getDuration() {
            return MySuperVideoView.this.mVodPlayer.getDuration();
        }

        public float getCurrentPlaybackTime() {
            return MySuperVideoView.this.mVodPlayer.getCurrentPlaybackTime();
        }

        public void seekTo(int position) {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                if (MySuperVideoView.this.mVodPlayer != null) {
                    MySuperVideoView.this.mVodPlayer.seek(position);
                }
            } else {
                MySuperVideoView.this.mCurrentPlayType = 3;
                MySuperVideoView.this.mVodControllerSmall.updatePlayType(3);
                MySuperVideoView.this.mVodControllerLarge.updatePlayType(3);
                LogReport.getInstance().uploadLogs("timeshift", 0L, 0);
                if (MySuperVideoView.this.mLivePlayer != null) {
                    MySuperVideoView.this.mLivePlayer.seek(position);
                }

                if (MySuperVideoView.this.mWatcher != null) {
                    MySuperVideoView.this.mWatcher.stop();
                }
            }

        }

        public boolean isPlaying() {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                return MySuperVideoView.this.mVodPlayer.isPlaying();
            } else {
                return MySuperVideoView.this.mCurrentPlayState == 1;
            }
        }

        public void onDanmuku(boolean on) {
            if (MySuperVideoView.this.mDanmuView != null) {
                MySuperVideoView.this.mDanmuView.toggle(on);
            }

        }

        public void onSnapshot() {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                if (MySuperVideoView.this.mVodPlayer != null) {
                    MySuperVideoView.this.mVodPlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                        public void onSnapshot(Bitmap bmp) {
                            MySuperVideoView.this.showSnapshotWindow(bmp);
                        }
                    });
                }
            } else if (MySuperVideoView.this.mLivePlayer != null) {
                MySuperVideoView.this.mLivePlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                    public void onSnapshot(Bitmap bmp) {
                        MySuperVideoView.this.showSnapshotWindow(bmp);
                    }
                });
            }

        }

        public void onQualitySelect(TCVideoQulity quality) {
            MySuperVideoView.this.mVodControllerLarge.updateVideoQulity(quality);
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                if (MySuperVideoView.this.mVodPlayer != null) {
                    if (quality.index == -1) {
                        float currentTime = MySuperVideoView.this.mVodPlayer.getCurrentPlaybackTime();
                        MySuperVideoView.this.mVodPlayer.stopPlay(true);
                        TXCLog.i("SuperPlayerView", "onQualitySelect quality.url:" + quality.url);
                        MySuperVideoView.this.mVodPlayer.setStartTime(currentTime);
                        MySuperVideoView.this.mVodPlayer.startPlay(quality.url);
                    } else {
                        TXCLog.i("SuperPlayerView", "setBitrateIndex quality.index:" + quality.index);
                        MySuperVideoView.this.mVodPlayer.setBitrateIndex(quality.index);
                    }
                }
            } else if (MySuperVideoView.this.mLivePlayer != null && !TextUtils.isEmpty(quality.url)) {
                int result = MySuperVideoView.this.mLivePlayer.switchStream(quality.url);
                if (result < 0) {
                    Toast.makeText(MySuperVideoView.this.getContext(), "切换" + quality.title + "清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MySuperVideoView.this.getContext(), "正在切换到" + quality.title + "...", Toast.LENGTH_SHORT).show();
                }
            }

            LogReport.getInstance().uploadLogs("change_resolution", 0L, 0);
        }

        public void onSpeedChange(float speedLevel) {
            if (MySuperVideoView.this.mVodPlayer != null) {
                MySuperVideoView.this.mVodPlayer.setRate(speedLevel);
            }

            LogReport.getInstance().uploadLogs("change_speed", 0L, 0);
        }

        public void onMirrorChange(boolean isMirror) {
            if (MySuperVideoView.this.mVodPlayer != null) {
                MySuperVideoView.this.mVodPlayer.setMirror(isMirror);
            }

            if (isMirror) {
                LogReport.getInstance().uploadLogs("mirror", 0L, 0);
            }

        }

        public void onHWAcceleration(boolean isAccelerate) {
            if (MySuperVideoView.this.mCurrentPlayType == 1) {
                MySuperVideoView.this.mChangeHWAcceleration = true;
                if (MySuperVideoView.this.mVodPlayer != null) {
                    MySuperVideoView.this.mVodPlayer.enableHardwareDecode(isAccelerate);
                    MySuperVideoView.this.mSeekPos = (int)MySuperVideoView.this.mVodPlayer.getCurrentPlaybackTime();
                    TXCLog.i("SuperPlayerView", "save pos:" + MySuperVideoView.this.mSeekPos);
                    MySuperVideoView.this.stopPlay();
                    MySuperVideoView.this.playVodURL(MySuperVideoView.this.mCurrentPlayVideoURL);
                }
            } else if (MySuperVideoView.this.mLivePlayer != null) {
                MySuperVideoView.this.mLivePlayer.enableHardwareDecode(isAccelerate);
                SuperPlayerModel modle=MySuperVideoView.this.mCurrentModelWrapper.requestModel;
                MySuperVideoView.this.playWithModel(modle);
            }

            if (isAccelerate) {
                LogReport.getInstance().uploadLogs("hw_decode", 0L, 0);
            } else {
                LogReport.getInstance().uploadLogs("soft_decode", 0L, 0);
            }

        }

        public void onFloatUpdate(int x, int y) {
            MySuperVideoView.this.mWindowParams.x = x;
            MySuperVideoView.this.mWindowParams.y = y;
            MySuperVideoView.this.mWindowManager.updateViewLayout(MySuperVideoView.this.mVodControllerFloat, MySuperVideoView.this.mWindowParams);
        }

        public void onReplay() {
            if (!TextUtils.isEmpty(MySuperVideoView.this.mCurrentPlayVideoURL)) {
                if (MySuperVideoView.this.isRTMPPlay(MySuperVideoView.this.mCurrentPlayVideoURL)) {
                    MySuperVideoView.this.playLiveURL(MySuperVideoView.this.mCurrentPlayVideoURL, 0);
                } else if (MySuperVideoView.this.isFLVPlay(MySuperVideoView.this.mCurrentPlayVideoURL)) {
                    MySuperVideoView.this.playLiveURL(MySuperVideoView.this.mCurrentPlayVideoURL, 1);
                } else {
                    MySuperVideoView.this.playVodURL(MySuperVideoView.this.mCurrentPlayVideoURL);
                }
            }

            if (MySuperVideoView.this.mVodControllerLarge != null) {
                MySuperVideoView.this.mVodControllerLarge.updateReplay(false);
            }

            if (MySuperVideoView.this.mVodControllerSmall != null) {
                MySuperVideoView.this.mVodControllerSmall.updateReplay(false);
            }

        }

        public void resumeLive() {
            if (MySuperVideoView.this.mLivePlayer != null) {
                MySuperVideoView.this.mLivePlayer.resumeLive();
            }

            MySuperVideoView.this.mVodControllerSmall.updatePlayType(2);
            MySuperVideoView.this.mVodControllerLarge.updatePlayType(2);
        }
    }
}
