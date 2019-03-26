package com.gin.ngeretail.telesales.Component.Service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.gin.ngeretail.telesales.Activity.CallCustomerActivity;
import com.gin.ngeretail.telesales.Component.Record.Record;
import com.gin.ngeretail.telesales.Data.ApiData;
import com.gin.ngeretail.telesales.Data.CallData;
import com.gin.ngeretail.telesales.Data.UserData;
import com.gin.ngeretail.telesales.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Tservice extends Service {

    private CallCustomerActivity.Calling calling;
    private static final String ACTION_OUT = "android.intent.action.PHONE_STATE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*
    Dipanggil oleh sistem setiap kali klien secara eksplisit memulai layanan dengan menelepon
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        this.calling = new CallCustomerActivity.Calling();
        this.registerReceiver(this.calling, filter);
        return START_NOT_STICKY;
    }

}
