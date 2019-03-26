package com.gin.ngeretail.telesales.Component.Service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.gin.ngeretail.telesales.BuildConfig;

public class CountdownService extends Service{
    public static final String COUNTDOWN_SERVICE_FINISH = "COUNTDOWN_SERVICE_FINISH";
    public static final long MILIS_IN_FUTURE_DEBUG = 2000;
    public static final long MILIS_IN_FUTURE = 5000;
    public static final long COUNTDOWN_INTERVAL = 2000;
    private CountDownTimer countDownTimer;

    public long getMilisInFuture() {
        if (BuildConfig.DEBUG) {
            return MILIS_IN_FUTURE_DEBUG;
        } else {
            return MILIS_IN_FUTURE;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.countDownTimer = new CountDownTimer(this.getMilisInFuture(), COUNTDOWN_INTERVAL) {
            public void onFinish(){
                Intent intent = new Intent(COUNTDOWN_SERVICE_FINISH);
                LocalBroadcastManager.getInstance(CountdownService.this).sendBroadcast(intent);
                countDownTimer.start();
            }
            public void onTick(long t){

            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }
}