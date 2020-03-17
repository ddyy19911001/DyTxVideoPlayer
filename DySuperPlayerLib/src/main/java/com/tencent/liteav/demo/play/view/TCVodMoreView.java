package com.tencent.liteav.demo.play.view;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.R.id;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;

public class TCVodMoreView extends RelativeLayout implements OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private SeekBar mSeekBarVolume;
    private SeekBar mSeekBarLight;
    private Switch mSwitchMirror;
    private Switch mSwitchAccelerate;
    private TCVodMoreView.Callback mCallback;
    private AudioManager mAudioManager;
    private RadioGroup mRadioGroup;
    private RadioButton mRbSpeed1;
    private RadioButton mRbSpeed125;
    private RadioButton mRbSpeed15;
    private RadioButton mRbSpeed2;
    private LinearLayout mLayoutSpeed;
    private LinearLayout mLayoutMirror;
    private OnSeekBarChangeListener mVolumeChangeListener;
    private OnSeekBarChangeListener mLightChangeListener;

    public TCVodMoreView(Context context) {
        super(context);
        this.mVolumeChangeListener = new NamelessClass_2();
        this.mLightChangeListener = new NamelessClass_1();
        this.init(context);
    }

    public TCVodMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mVolumeChangeListener = new NamelessClass_2();
        this.mLightChangeListener = new NamelessClass_1();
        this.init(context);
    }

    public class NamelessClass_2 implements OnSeekBarChangeListener {
        NamelessClass_2() {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            TCVodMoreView.this.updateVolumeProgress(progress);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    public class NamelessClass_1 implements OnSeekBarChangeListener {
        NamelessClass_1() {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            TCVodMoreView.this.updateBrightProgress(progress);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    public TCVodMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);



        this.mVolumeChangeListener = new NamelessClass_2();



        this.mLightChangeListener = new NamelessClass_1();
        this.init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(this.mContext).inflate(R.layout.player_more_popup_view, this);
        this.mLayoutSpeed = (LinearLayout)this.findViewById(id.layout_speed);
        this.mRadioGroup = (RadioGroup)this.findViewById(id.radioGroup);
        this.mRbSpeed1 = (RadioButton)this.findViewById(id.rb_speed1);
        this.mRbSpeed125 = (RadioButton)this.findViewById(id.rb_speed125);
        this.mRbSpeed15 = (RadioButton)this.findViewById(id.rb_speed15);
        this.mRbSpeed2 = (RadioButton)this.findViewById(id.rb_speed2);
        this.mRadioGroup.setOnCheckedChangeListener(this);
        this.mSeekBarVolume = (SeekBar)this.findViewById(id.seekBar_audio);
        this.mSeekBarLight = (SeekBar)this.findViewById(id.seekBar_light);
        this.mLayoutMirror = (LinearLayout)this.findViewById(id.layout_mirror);
        this.mSwitchMirror = (Switch)this.findViewById(id.switch_mirror);
        this.mSwitchAccelerate = (Switch)this.findViewById(id.switch_accelerate);
        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
        this.mSwitchAccelerate.setChecked(config.enableHWAcceleration);
        this.mSeekBarVolume.setOnSeekBarChangeListener(this.mVolumeChangeListener);
        this.mSeekBarLight.setOnSeekBarChangeListener(this.mLightChangeListener);
        this.mSwitchMirror.setOnCheckedChangeListener(this);
        this.mSwitchAccelerate.setOnCheckedChangeListener(this);
        this.mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        this.updateCurrentVolume();
        this.updateCurrentLight();
    }

    private void updateCurrentVolume() {
        int curVolume = this.mAudioManager.getStreamVolume(3);
        int maxVolume = this.mAudioManager.getStreamMaxVolume(3);
        float percentage = (float)curVolume / (float)maxVolume;
        int progress = (int)(percentage * (float)this.mSeekBarVolume.getMax());
        this.mSeekBarVolume.setProgress(progress);
    }

    private void updateCurrentLight() {
        Activity activity = (Activity)this.mContext;
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (params.screenBrightness == -1.0F) {
            params.screenBrightness = getActivityBrightness((Activity)this.mContext);
            window.setAttributes(params);
            if (params.screenBrightness == -1.0F) {
                this.mSeekBarLight.setProgress(100);
                return;
            }

            this.mSeekBarLight.setProgress((int)(params.screenBrightness * 100.0F));
        }

    }

    public static float getActivityBrightness(Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams params = localWindow.getAttributes();
        return params.screenBrightness;
    }

    private void updateVolumeProgress(int progress) {
        float percentage = (float)progress / (float)this.mSeekBarVolume.getMax();
        if (percentage >= 0.0F && percentage <= 1.0F) {
            if (this.mAudioManager != null) {
                int maxVolume = this.mAudioManager.getStreamMaxVolume(3);
                int newVolume = (int)(percentage * (float)maxVolume);
                this.mAudioManager.setStreamVolume(3, newVolume, 0);
            }

        }
    }

    private void updateBrightProgress(int progress) {
        Activity activity = (Activity)this.mContext;
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = (float)progress * 1.0F / 100.0F;
        if (params.screenBrightness > 1.0F) {
            params.screenBrightness = 1.0F;
        }

        if (params.screenBrightness <= 0.01F) {
            params.screenBrightness = 0.01F;
        }

        window.setAttributes(params);
        this.mSeekBarLight.setProgress(progress);
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == id.switch_mirror) {
            if (this.mCallback != null) {
                this.mCallback.onMirrorChange(isChecked);
            }
        } else if (compoundButton.getId() == id.switch_accelerate) {
            SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
            config.enableHWAcceleration = !config.enableHWAcceleration;
            this.mSwitchAccelerate.setChecked(config.enableHWAcceleration);
            if (this.mCallback != null) {
                this.mCallback.onHWAcceleration(config.enableHWAcceleration);
            }
        }

    }

    public void setCallback(TCVodMoreView.Callback callback) {
        this.mCallback = callback;
    }

    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == id.rb_speed1) {
            this.mRbSpeed1.setChecked(true);
            if (this.mCallback != null) {
                this.mCallback.onSpeedChange(1.0F);
            }
        } else if (checkedId == id.rb_speed125) {
            this.mRbSpeed125.setChecked(true);
            if (this.mCallback != null) {
                this.mCallback.onSpeedChange(1.25F);
            }
        } else if (checkedId == id.rb_speed15) {
            this.mRbSpeed15.setChecked(true);
            if (this.mCallback != null) {
                this.mCallback.onSpeedChange(1.5F);
            }
        } else if (checkedId == id.rb_speed2) {
            this.mRbSpeed2.setChecked(true);
            if (this.mCallback != null) {
                this.mCallback.onSpeedChange(2.0F);
            }
        }

    }

    public void updatePlayType(int playType) {
        if (playType == 1) {
            this.mLayoutSpeed.setVisibility(VISIBLE);
//            this.mLayoutMirror.setVisibility(VISIBLE);
        } else {
            this.mLayoutSpeed.setVisibility(GONE);
//            this.mLayoutMirror.setVisibility(GONE);
        }

    }

    public interface Callback {
        void onSpeedChange(float var1);

        void onMirrorChange(boolean var1);

        void onHWAcceleration(boolean var1);
    }
}
