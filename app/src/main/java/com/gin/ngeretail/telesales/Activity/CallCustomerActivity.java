package com.gin.ngeretail.telesales.Activity;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.gin.ngeretail.telesales.Base.Common;
import com.gin.ngeretail.telesales.Component.Service.CountdownService;
import com.gin.ngeretail.telesales.Component.Service.Tservice;
import com.gin.ngeretail.telesales.Component.UI.CustomAlert;
import com.gin.ngeretail.telesales.Component.Record.Record;
import com.gin.ngeretail.telesales.Component.UI.LeftMenuModule;
import com.gin.ngeretail.telesales.Data.ApiData;
import com.gin.ngeretail.telesales.Data.CallData;
import com.gin.ngeretail.telesales.Data.Global;
import com.gin.ngeretail.telesales.Data.UserData;
import com.gin.ngeretail.telesales.Data.SharedPreferenceData;
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

public class CallCustomerActivity extends AppCompatActivity
        implements LeftMenuModule.ILeftMenu {

    private LinearLayout layoutNoTask, layoutCallTask;
    private TextView tvCustName, tvPhoneNumber;
    private Button btnCall;
    private static AsyncHttpClient httpClient;
    private LeftMenuModule leftMenuModule;
    private static UserData userData;
    private CustomAlert alert;
    private static CustomAlert alertInfoError;
    public static List<CallData> callDataList = new ArrayList<>();
    private List<SubscriptionInfo> sbInfo;
    private SubscriptionManager mSubscriptionManager;
    private TelecomManager telecomManager;
    private List<PhoneAccountHandle> phoneAccountHandleList;
    private SubscriptionInfo infoSim1;
    private SubscriptionInfo infoSim2;
    private PhoneAccountHandle phoneAccountHandleListSim1;
    private PhoneAccountHandle phoneAccountHandleListSim2;
    private static SharedPreferenceData sharedPreferenceData;
    private String checkSim;
    private String error_get_data;
    private static String error_upload;
    public static String filename;
    private static Record record;
    private static Context context;
    private static CallCustomerActivity callCustomerActivity;
    private String memorySize;
    private double size;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    public static final String USER_DATA_CODE = "USER_DATA_CODE";
    public static final String CALL_DATA_DODE = "CALL_DATA_DODE";
    public static CallCustomerActivity cd;
    private static Global global;
    private static ProgressDialog progressDialog;
    public static final int START_CALL_CODE = 1;
    public static final int END_CALL_CODE = 2;
    public static final int MINIMUM_SDK_VERSION = 23;
    public static final int PERMISSION_CALL_CODE = 123;
    public static final int PERMISSION_STORAGE_CODE = 321;
    public static final int ALL_PERMISSION_CODE = 111;
    public static final String ACTION_OUT = "android.intent.action.PHONE_STATE";

    //region override function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.drawer_layout_call_customer);
            global.checkForDebugOrRelease();
            //info();
            getDataFromIntent();
            init();

            callCustomerActivity = this;

            if (!isUserDataNull()) {
                getTaskCall(userData.emailUser);
                chooseSim();
            }

            startService(new Intent(CallCustomerActivity.this, CountdownService.class));
            startService(new Intent(this, Tservice.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(CallCustomerActivity.this, CountdownService.class));
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(countdownBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(CountdownService.COUNTDOWN_SERVICE_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(countdownBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver countdownBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onFinishCountdownService();
        }
    };

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener okListener = (dialog, which) -> moveTaskToBack(true);

        alert.showAskDialog("Keluar dari aplikasi? \n\n", okListener);
    }

    @Override
    public boolean onMenuItemSelected(int itemId) throws Exception {
        if (itemId == R.id.menu_logout) {
            showAskDialogToLogout();
        }
        return false;
    }

    @Override
    public void onHandleBackPressed() {
        leftMenuModule.closeDrawer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_PERMISSION_CODE:
                getCallDuration(getApplicationContext());
                break;
            case PERMISSION_STORAGE_CODE:
                Calling.deleteLastCallLog(context, callDataList.get(0).szCustPhone);
                break;
        }
    }

    //endregion

    //region async
    private void getTaskCall(String employeeId) {
        ApiData.Authorize(httpClient, userData.tokenId);
        String url = global.BASE_URL + "DetailData/GetCall?employeeId=" + employeeId;
        //httpClient.setTimeout(5000);
        httpClient.get(this, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                callDataList = setCallData(result);
                try {
                    if (callDataList == null || callDataList.size() == 0) {
                        layoutNoTask.setVisibility(View.VISIBLE);
                        layoutCallTask.setVisibility(View.GONE);
                        btnCall.setVisibility(View.GONE);
                    } else {
                        setDataToUI(callDataList.get(0));
                        layoutNoTask.setVisibility(View.GONE);
                        layoutCallTask.setVisibility(View.VISIBLE);
                        btnCall.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    error_get_data= (String)sharedPreferenceData.read(UserData.ERROR, SharedPreferenceData.STRING);
                            if(error_get_data.equals("error")){
                                return;
                            }
                            else{
                                sharedPreferenceData.write(UserData.ERROR, "error");
                                alertInfoError.showError("Error Pada GetTask Call", (Exception) error, null);
                            }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void startOrEndCall(String employeeId, String custId, int type, String phoneNumber, double duration) {
        ApiData.Authorize(httpClient, userData.tokenId);
        String url = setUrlForCall(type, employeeId, custId, duration);
        //httpClient.setTimeout(5000);
        //httpClient.setMaxRetriesAndTimeout(2,10000);
        httpClient.get(context, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(type == END_CALL_CODE){
                    Toast.makeText(getActivityInstance(),"Status Berhasil Terupdate",Toast.LENGTH_SHORT).show();
                    sharedPreferenceData.write(UserData.ERROR, "");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    if(type == START_CALL_CODE){
                        alertInfoError.showError(error.getMessage(), (Exception) error, null);
                    }
                     else if(type == END_CALL_CODE){
                        alertInfoError.showError("Status Tidak Berhasil Terupdate", (Exception) error, null);
                        sharedPreferenceData.write(UserData.ERROR, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private static String setUrlForCall(int type, String employeeId, String custId, double duration) {
        if (type == START_CALL_CODE) {
            return global.BASE_URL + "DetailData/StartCall?employeeId=" + employeeId + "&custId=" + custId;
        } else {
            return global.BASE_URL + "DetailData/EndCall?employeeId=" + employeeId + "&custId=" + custId + "&duration=" + duration;
        }
    }

    //endregion

    //region other function

    private static CallCustomerActivity getActivityInstance() {
        return callCustomerActivity;
    }

    private void setDataToUI(CallData data) {
        tvCustName.setText(data.szCustName);
        //tvPhoneNumber.setText(data.szCustPhone);
    }

    private boolean isUserDataNull() {
        if (userData == null) {
            return true;
        }
        return false;
    }

    private List<CallData> setCallData(String result) {
        List<CallData> callData = new ArrayList<>();
        Gson gson = new Gson();

        callData = gson.fromJson(result, new TypeToken<List<CallData>>() {
        }.getType());

        return callData;
    }

    private void getDataFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userData = (UserData) bundle.getSerializable(USER_DATA_CODE);
        }
    }

    private void callingCustomer(CallData data) {
        if (Build.VERSION.SDK_INT < MINIMUM_SDK_VERSION) {
            dialPhoneNumber(data);
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                dialPhoneNumber(data);

            } else {
                final String[] PERMISSIONS = {Manifest.permission.CALL_PHONE};
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CALL_CODE);
            }
        }
    }

    private void dialPhoneNumber(CallData callData) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
            String currentDateandTime = sdf.format(new Date());
            filename = currentDateandTime + "_" + userData.emailUser + "_" + callData.szCustId.toString() + ".mp3";

            Intent call = new Intent(Intent.ACTION_CALL);
            call.setData(Uri.parse("tel:" + callData.szCustPhone));
            call.putExtra("com.android.phone.force.slot", true);
            size = Double.parseDouble(memorySize);
            checkSim= (String) sharedPreferenceData.read(UserData.CHOSEE_SIM, SharedPreferenceData.STRING);
            if(sbInfo.size() > 1){
                if(size > 25){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(checkSim.equals("1")){
                            call.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleListSim1);
                        }
                        else{
                            call.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleListSim2);
                        }
                        startOrEndCall(callData.szEmployeeId, callData.szCustId, START_CALL_CODE, callData.szCustPhone, 0);
                        startActivity(call);
                    }
                }
                else{
                    alert.showToastMessage("Memori Full Maaf Anda Harus Menghapus\n Data Memori Internal Anda Harus Diatas 25MB");
                }
            }
            else {
                if(size > 25){
                    startOrEndCall(callData.szEmployeeId, callData.szCustId, START_CALL_CODE, callData.szCustPhone, 0);
                    startActivity(call);
                }
                else {
                    alert.showToastMessage("Memori Full Maaf Anda Harus Menghapus\n Data Memori Internal Anda Harus Diatas 25MB");
                }
            }

        } else {
            alert.showToastMessage("You don't assign permission.");
        }
    }

    private void onFinishCountdownService() {
        try {
            getTaskCall(userData.emailUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAskDialogToLogout() {
        DialogInterface.OnClickListener okListener = (dialog, which) -> doLogout();

        String message = "Logout dari akun Anda? \n\n";
        alert.showAskDialog(message, okListener);
    }

    private void doLogout() {
        SharedPreferenceData sharedPreferenceData = new SharedPreferenceData(UserData.FILE_NAME, this);
        sharedPreferenceData.write(UserData.KEY_LOGIN_STATUS, false);
        sharedPreferenceData.write(UserData.KEY_TOKEN, "");
        sharedPreferenceData.write(UserData.CHOSEE_SIM, "");
        sharedPreferenceData.write(UserData.PROFILE_PIC,"");
        sharedPreferenceData.write(UserData.KEY_EMAIL, "");
        Intent intent = new Intent(this, LoginTelesalesActivity.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.telesales_id_new))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut();
        auth.signOut();
        finish();
        startActivity(intent);

    }

    private void signOut() {
        auth.signOut();
        SharedPreferenceData sharedPreferenceData = new SharedPreferenceData(UserData.FILE_NAME, this);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sharedPreferenceData.write(UserData.KEY_LOGIN_STATUS, false);
                        sharedPreferenceData.write(UserData.KEY_TOKEN, "");
                        sharedPreferenceData.write(UserData.KEY_EMAIL, "");
                        Intent intent = new Intent(CallCustomerActivity.this, LoginTelesalesActivity.class);
                        startActivity(intent);
                        //
                    }
                });
    }

    //endregion

    //region init function
    private void init() {
        layoutCallTask = findViewById(R.id.layoutCallTask);
        layoutNoTask = findViewById(R.id.layoutNoTask);
        btnCall = findViewById(R.id.btnCallNow);

        ImageView imageNoTask = findViewById(R.id.layout_image_view_no_task);

        tvCustName = findViewById(R.id.tvCustName);
        //tvPhoneNumber = findViewById(R.id.tvPhoneNum);
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        auth = FirebaseAuth.getInstance();
        initToolbar(toolbar);
        initLeftMenuModule(toolbar);
        initTelecom();
        initObject();
        memorySize = record.FreeExtMemory();
        btnCall.setOnClickListener(v -> callingCustomer(callDataList.get(0)));
    }

    private void initTelecom() {
        telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            } else {
                final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE,};
                ActivityCompat.requestPermissions(CallCustomerActivity.this, PERMISSIONS, ALL_PERMISSION_CODE);
            }
        }
    }

    private void initToolbar(Toolbar toolbar) {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private void initLeftMenuModule(Toolbar toolbar) {
        leftMenuModule = new LeftMenuModule(CallCustomerActivity.this, toolbar);
        leftMenuModule.setLeftMenuListener(this);
        leftMenuModule.setProfileInfo(userData);
        leftMenuModule.setAppVersion(Common.GetVersionInfo(this));
    }


    private void initObject() {
        httpClient = new AsyncHttpClient();
        alert = new CustomAlert(this);
        alertInfoError = new CustomAlert(this);
        mSubscriptionManager = SubscriptionManager.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            sbInfo = mSubscriptionManager.getActiveSubscriptionInfoList();
        }
        else {
            final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE};
            ActivityCompat.requestPermissions(getActivityInstance(), PERMISSIONS, ALL_PERMISSION_CODE);
        }
        record = new Record();
        File FF = new File(Environment.getExternalStorageDirectory() + "/" + "RecordTelesalesVoice");
        record.deleteFileRecords(FF);
        sharedPreferenceData = new SharedPreferenceData(UserData.FILE_NAME, this);
    }

    //endregion

    //region phone

    public static String getCallDuration(Context applicationContext) {
        String durationInSecond = "";
        int duration;

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = applicationContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 1;");
            duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
            while (cursor.moveToNext()) {
                durationInSecond = cursor.getString(duration);
            }
            cursor.close();

        } else {
            final String[] PERMISSIONS = {Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS};
            ActivityCompat.requestPermissions(getActivityInstance(), PERMISSIONS, ALL_PERMISSION_CODE);
        }
        return durationInSecond;
    }

//    private void showCallBy2Sim(Intent intent, CallData data){
//        String titile= getResources().getString(R.string.choose_sim_card);
//        final CharSequence[] items = {
//              "SIM 1"+"("+infoSim1.getCarrierName().toString()+")"+"",
//                "SIM 2"+"("+infoSim2.getCarrierName().toString()+")"+""
//        };
//        DialogInterface.OnClickListener okListener = gotoChooseSimCard(intent, data);
//        alert.showAskDialogItems(titile,items,okListener);
//    }

//    public DialogInterface.OnClickListener gotoChooseSimCard(Intent intent, CallData data){
//        return new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                try {
//                    switch (which){
//                        case 0:
//                            if(phoneAccountHandleList != null && phoneAccountHandleList.size()>0){
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
//                                }
//
//                                startOrEndCall(data.szEmployeeId, data.szCustId, START_CALL_CODE, data.szCustPhone, 0);
//                                startActivity(intent);
//                            }
//                            break;
//                        case 1:
//                            if(phoneAccountHandleList != null && phoneAccountHandleList.size()>1){
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
//                                }
//                                startOrEndCall(data.szEmployeeId, data.szCustId, START_CALL_CODE, data.szCustPhone, 0);
//                                startActivity(intent);
//                            }
//                            break;
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//    }

    public static void UploadMp3(byte[] records, String file){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String content_Type= ".mp3";
                OkHttpClient client = new OkHttpClient();
                RequestBody file_body= RequestBody.create(MediaType.parse(content_Type), records);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", content_Type)
                        .addFormDataPart("voiceFile", file, file_body)
                        .build();
                Request request = new Request.Builder().
                        url(global.BASE_URL + "DetailData/UploadFilesToDatabase")
                        .addHeader("Content-Type", "multipart/form-data")
                        .addHeader("responseType","json")
                        .addHeader("token", userData.tokenId)
                        .addHeader("appId","telesales")
                        .post(requestBody)
                        .build();
                try{
                    //startProgressDialog(ProgressDialog.STYLE_SPINNER);
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful()){
                        //Toast.makeText(getApplicationContext(), "error",Toast.LENGTH_SHORT).show();
                        //dismissProgressDialog();
                        sharedPreferenceData.write(UserData.RETRY_UPLOAD_MP3, "");
                    }
                    else{
                        sharedPreferenceData.write(UserData.RETRY_UPLOAD_MP3, "upload");
                    }
                }catch (IOException e){
                    alertInfoError.showError("Error Pada Voice Call", (Exception) e, null);
                }
            }
        });
        t.start();
    }


    public static class Calling extends BroadcastReceiver {
        Bundle bundle;
        String state;
        public boolean wasRinging = false;
        private byte[] records = new byte[0];
        private boolean isRecorder = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        wasRinging = true;
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (!wasRinging) {
                            record.startRecoder(filename);
                            isRecorder = true;
                        }
                        wasRinging = true;
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        if (wasRinging) {
                            if (isRecorder) {
                                try {
                                    record.stopRecording();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(() -> {
                                        String durationInSecond = getCallDuration(context);
                                       String phoneNumber = CallLog.Calls.getLastOutgoingCall(context);
                                       double duration = Double.parseDouble(durationInSecond);
                                        if (duration > 0.00) {
                                            //startProgressDialog(ProgressDialog.STYLE_SPINNER);
                                            records = record.fileToBytes(filename);
                                            UploadMp3(records, filename);
                                            error_upload= (String)sharedPreferenceData.read(UserData.RETRY_UPLOAD_MP3, SharedPreferenceData.STRING);
                                            if(error_upload.equals("upload")){
                                                UploadMp3(records, filename);
                                            }
                                            else{
                                                Toast.makeText(getActivityInstance(),"Upload Record Sukses",Toast.LENGTH_SHORT).show();
                                                //dismissProgressDialog();
                                            }
                                        } else {
                                            File FF = new File(Environment.getExternalStorageDirectory() + "/" + "RecordTelesalesVoice");
                                            record.deleteFileRecords(FF);
                                        }

                                        if(callDataList.get(0).szCustPhone.trim().equals(phoneNumber.trim())){
                                            startOrEndCall(callDataList.get(0).szEmployeeId, callDataList.get(0).szCustId, END_CALL_CODE, phoneNumber, duration);
                                            deleteLastCallLog(context,phoneNumber);
                                        }
                                    }, 1500);
                                    wasRinging = false;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }
        }

        public static void deleteLastCallLog(Context context, String phoneNumber) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                try {
                    String queryString = "NUMBER = '" + phoneNumber + "'";
                    context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);

                } catch (Exception e) {
                    alertInfoError.showError("Error Delete Call log", (Exception) e, null);
                    //e.printStackTrace();
                }
            }
            else {
                final String[] PERMISSION_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_CALL_LOG};
                ActivityCompat.requestPermissions(getActivityInstance(), PERMISSION_STORAGE, PERMISSION_STORAGE_CODE);
            }
        }
    }

//    public static Bitmap getCircleBitmap(Bitmap bm) {
//        Bitmap output = null;
//        if(bm != null){
//            int sice = Math.min((bm.getWidth()), (bm.getHeight()));
//
//            Bitmap bitmap = ThumbnailUtils.extractThumbnail(bm, sice, sice);
//
//            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//
//            Canvas canvas = new Canvas(output);
//
//            final int color = 0xffff0000;
//            final Paint paint = new Paint();
//            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//            final RectF rectF = new RectF(rect);
//
//            paint.setAntiAlias(true);
//            paint.setDither(true);
//            paint.setFilterBitmap(true);
//            canvas.drawARGB(0, 0, 0, 0);
//            paint.setColor(color);
//            canvas.drawOval(rectF, paint);
//
//            paint.setColor(Color.BLUE);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth((float) 4);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(bitmap, rect, rect, paint);
//        }
//        return output;
//    }

    private void chooseSim(){
        if(userData.sim.equals("1")){
            phoneAccountHandleListSim1 = phoneAccountHandleList.get(0);
        }else if(userData.sim.equals("2")){
            phoneAccountHandleListSim2 = phoneAccountHandleList.get(1);
        }
        else {
            if (sbInfo.size() == 1) {
            }
            else{
                infoSim1 = sbInfo.get(0);
                infoSim2 = sbInfo.get(1);
                showCallBy2SimNew();
            }
        }
    }

    private void showCallBy2SimNew(){
        String titile= getResources().getString(R.string.choose_sim_card);
        final CharSequence[] items = {
                "SIM 1"+"("+infoSim1.getCarrierName().toString()+")"+"",
                "SIM 2"+"("+infoSim2.getCarrierName().toString()+")"+""
        };
        DialogInterface.OnClickListener okListener = gotoChooseSimCardNew();
        alert.showAskDialogItems(titile,items,okListener);
    }

    public DialogInterface.OnClickListener gotoChooseSimCardNew(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    switch (which){
                        case 0:
                            if(phoneAccountHandleList != null && phoneAccountHandleList.size()>0){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    sharedPreferenceData.write(UserData.CHOSEE_SIM, "1");
                                    //intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
                                    phoneAccountHandleListSim1 = phoneAccountHandleList.get(0);


                                }
                            }
                            break;
                        case 1:
                            if(phoneAccountHandleList != null && phoneAccountHandleList.size()>1){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    //intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
                                    sharedPreferenceData.write(UserData.CHOSEE_SIM, "2");
                                    phoneAccountHandleListSim2 = phoneAccountHandleList.get(1);
                                }
                            }
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void startProgressDialog(int type) {
        progressDialog = new ProgressDialog(getActivityInstance());
        progressDialog.setTitle("Mohon tunggu");
        progressDialog.setMessage("Sedang Mengupload Record");
        progressDialog.setProgressStyle(type);
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    //endregion
}