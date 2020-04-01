package com.tencent.liteav.demo.play.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class VoiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION) ) {
            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            final int ringerMode = am.getRingerMode();
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    //normal
                    Log.i("静音：", "普通模式");
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    //vibrate
                    Log.i("静音：", "振动模式");
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    //silent
                    Log.i("静音：", "已被静音");
                    break;
            }
        }else if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")&&
                (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == AudioManager.STREAM_MUSIC)){
            Log.i("静音：", "音量发生变化");
            if(voiceListener!=null){
                voiceListener.onVoiceChange(getCurrentMusicVolume(context));
            }
        }
    }

    /**
       * 获取当前媒体音量
       * @return
       */
    public int getCurrentMusicVolume(Context context) {
        AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager != null ? mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : -1;
   }


    VoiceListener voiceListener;

    public void setVoiceListener(VoiceListener voiceListener) {
         this.voiceListener=voiceListener;
    }

    public interface VoiceListener{
        void onVoiceChange(int volume);
   }
}
