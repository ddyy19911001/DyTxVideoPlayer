# DyTxVideoPlayer
# 使用方法

# 1.下载库文件导入modle
  
# 2.在app里面的glide中添加
  
    allprojects {
        repositories {
            flatDir {
                dirs 'libs'
            }
        }
      }
      
 # 3.将库文件里面的LiteAVSDK_Player.aar文件拷贝到libs里面
     

     
 # 4.Manifest文件里面的播放页面activity配置全屏
  
       <activity android:name=".MainActivity"
            android:configChanges="orientation|keyboardHidden|screenSize"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="stateHidden"
            >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
   # 5.就可以直接用MySuperVideoView进行布局，注意，这个外层必须用RelativieLayout进行包裹（也可以直接使用default_video_layout.xml代替视频播放器布局，高度修改可以直接写对应的demis名字改成一样就行）
