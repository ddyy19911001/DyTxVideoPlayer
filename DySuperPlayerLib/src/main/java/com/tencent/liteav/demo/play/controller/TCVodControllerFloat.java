package com.tencent.liteav.demo.play.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.tencent.liteav.demo.play.R.id;
import com.tencent.liteav.demo.play.R.layout;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig.TXRect;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.reflect.Field;

public class TCVodControllerFloat extends TCVodControllerBase implements OnClickListener {
    private TXCloudVideoView floatVideoView;
    private static int statusBarHeight;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private float xInView;
    private float yInView;

    public TCVodControllerFloat(Context context) {
        super(context);
        this.initViews();
    }

    public TCVodControllerFloat(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initViews();
    }

    public TCVodControllerFloat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initViews();
    }

    private void initViews() {
        this.mLayoutInflater.inflate(layout.vod_controller_float, this);
        this.floatVideoView = (TXCloudVideoView)this.findViewById(id.float_cloud_video_view);
        ImageView ivClose = (ImageView)this.findViewById(id.iv_close);
        ivClose.setOnClickListener(this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case 0:
                this.xInView = event.getX();
                this.yInView = event.getY();
                this.xDownInScreen = event.getRawX();
                this.yDownInScreen = event.getRawY() - (float)this.getStatusBarHeight();
                this.xInScreen = event.getRawX();
                this.yInScreen = event.getRawY() - (float)this.getStatusBarHeight();
                break;
            case 1:
                if (this.xDownInScreen == this.xInScreen && this.yDownInScreen == this.yInScreen && this.mVodController != null) {
                    this.mVodController.onRequestPlayMode(1);
                }
                break;
            case 2:
                this.xInScreen = event.getRawX();
                this.yInScreen = event.getRawY() - (float)this.getStatusBarHeight();
                this.updateViewPosition();
        }

        return true;
    }

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer)field.get(o);
                statusBarHeight = this.getResources().getDimensionPixelSize(x);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return statusBarHeight;
    }

    private void updateViewPosition() {
        int x = (int)(this.xInScreen - this.xInView);
        int y = (int)(this.yInScreen - this.yInView);
        TXRect rect = SuperPlayerGlobalConfig.getInstance().floatViewRect;
        if (rect != null) {
            rect.x = x;
            rect.y = y;
        }

        if (this.mVodController != null) {
            this.mVodController.onFloatUpdate(x, y);
        }

    }

    void onShow() {
    }

    void onHide() {
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == id.iv_close && this.mVodController != null) {
            this.mVodController.onBackPress(3);
        }

    }

    public TXCloudVideoView getFloatVideoView() {
        return this.floatVideoView;
    }
}
