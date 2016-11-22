package cn.edu.pku.wangjie.miniweather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    int counter = 0;
    static  final int UPDATE_INTERVAL =1000*60*60;
    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doSomethingRepeatedly();
        Log.d("myService","onStartCommand" );
        return START_STICKY;
    }

    private void doSomethingRepeatedly() {

        //定时器一个小时发送一次广播
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("MyService",String.valueOf(++counter));

                //向MainActivity广播
                Intent intent = new Intent();
                intent.setAction("UPDATE_TODAY_WEATHER");   //自定义行为
                sendBroadcast(intent);
            }
        },0,UPDATE_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
