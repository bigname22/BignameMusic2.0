package com.example.administrator.bignamemusic20;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Bigname on 2016/9/27.
 */
public class MusicService extends Service {
    MediaPlayer mediaplyer;
    @Override
    public void onCreate() {
        Log.d("bigname_log", "onCreate: 服务创建");
        super.onCreate();
        mediaplyer = new MediaPlayer();
    }
    public void intiPlyer(){

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mediaplyer == null) {
            mediaplyer = new MediaPlayer();
        }
        return new MyBinder();
    }

    class MyBinder extends Binder{
        public MediaPlayer getMediaPlay() {
            return mediaplyer;
        }
        public MusicService getMusicService(){
            return MusicService.this;
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("bigname_log", "onDestroy: 服务销毁");
    }
}
